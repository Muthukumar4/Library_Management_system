const BASE_URL = "http://localhost:8080";

const NOTIFY_KEY = "lmsAdminNotifications";

let isDashboardOverviewLoading = false;



function animateCounter(el, targetValue, duration = 700) {

  if (!el) return;

  const target = Number(targetValue) || 0;

  const start = Number(el.dataset.count || el.textContent || 0) || 0;

  const startTime = performance.now();



  function frame(now) {

    const p = Math.min(1, (now - startTime) / duration);

    const val = Math.round(start + (target - start) * p);

    el.textContent = String(val);

    if (p < 1) requestAnimationFrame(frame);

    else el.dataset.count = String(target);

  }

  requestAnimationFrame(frame);

}



function formatNowTime() {

  const d = new Date();

  return d.toLocaleString();

}



function getNotifications() {

  try {

    const raw = localStorage.getItem(NOTIFY_KEY);

    const parsed = raw ? JSON.parse(raw) : [];

    return Array.isArray(parsed) ? parsed : [];

  } catch (err) {

    return [];

  }

}



function saveNotifications(list) {

  localStorage.setItem(NOTIFY_KEY, JSON.stringify(list.slice(0, 50)));

}



function addNotification(message, type = "info") {

  if (!message || !String(message).trim()) return;

  const normalizedMessage = String(message).trim();

  const normalizedType = String(type || "info").trim().toLowerCase();

  const list = getNotifications();

  const latest = list.length ? list[0] : null;

  if (latest && latest.message === normalizedMessage && latest.type === normalizedType) {

    // Avoid repeated duplicates in a row, keep the latest timestamp.

    latest.time = formatNowTime();

    saveNotifications(list);

    renderNotifications();

    return;

  }

  list.unshift({ message: normalizedMessage, type: normalizedType, time: formatNowTime() });

  saveNotifications(list);

  renderNotifications();

}



function renderNotifications() {

  const listEl = document.getElementById("notificationList");

  const countEl = document.getElementById("notifyCount");

  const list = getNotifications();

  if (countEl) countEl.textContent = String(list.length);

  if (!listEl) return;

  if (!list.length) {

    listEl.innerHTML = "<li>No notifications yet.</li>";

    return;

  }

  listEl.innerHTML = list.map(item => `

    <li>

      ${escapeHtml(item.message)}

      <span class="notification-time">${escapeHtml(item.time || "-")}</span>

    </li>

  `).join("");

}



function toggleNotifications() {

  document.getElementById("notificationsPanel")?.classList.toggle("show");

}



function hideNotifications() {

  document.getElementById("notificationsPanel")?.classList.remove("show");

}



function clearNotifications() {

  saveNotifications([]);

  renderNotifications();

}



function showSuccessPopup(message, shouldReload = false) {

  addNotification(message, "success");

  if (window.Swal) {

    Swal.fire({

      icon: "success",

      title: "Success",

      text: message,

      confirmButtonColor: "#2563eb"

    }).then(() => {

      if (shouldReload) window.location.reload();

    });

    return;

  }

  alert(message);

  if (shouldReload) window.location.reload();

}



function showErrorPopup(message) {

  addNotification(message, "error");

  if (window.Swal) {

    Swal.fire({

      icon: "error",

      title: "Error",

      text: message,

      confirmButtonColor: "#dc2626"

    });

    return;

  }

  alert(message);

}

function isValidPhoneNumber(phone) {

  return /^\d{10}$/.test(phone);

}

function isValidEmailAddress(email) {

  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);

}



function setGlobalLoading(isLoading, text = "Please wait...") {

  const loader = document.getElementById("globalLoader");

  const loaderText = document.getElementById("globalLoaderText");

  if (loaderText) loaderText.textContent = text;

  if (!loader) return;

  loader.classList.toggle("show", Boolean(isLoading));

}



async function confirmAction(title, text, confirmButtonText = "Confirm") {

  if (window.Swal) {

    const result = await Swal.fire({

      icon: "question",

      title,

      text,

      showCancelButton: true,

      confirmButtonText,

      cancelButtonText: "Cancel",

      confirmButtonColor: "#2563eb"

    });

    return result.isConfirmed;

  }

  return window.confirm(`${title}\n${text}`);

}



function makeStatusBadge(type, label) {

  return `<span class="status-badge ${type}">${label}</span>`;

}



function makeEmptyState(title, hint = "", icon = "📭", actionLabel = "", actionSectionId = "") {

  const actionHtml = actionLabel && actionSectionId

    ? `<button type="button" class="cta" onclick="openSectionById('${actionSectionId}')">${escapeHtml(actionLabel)}</button>`

    : "";

  return `

    <div class="empty-state">

      <div class="icon">${icon}</div>

      <div class="title">${escapeHtml(title)}</div>

      <div class="hint">${escapeHtml(hint)}</div>

      ${actionHtml}

    </div>

  `;

}



function makeEmptyStateRow(colspan, title, hint = "", icon = "📭", actionLabel = "", actionSectionId = "") {

  return `<tr><td colspan="${Number(colspan) || 1}">${makeEmptyState(title, hint, icon, actionLabel, actionSectionId)}</td></tr>`;

}



function setTableLoadError(tbody, colspan, title, hint, icon = "⚠️") {
  if (!tbody) return;
  tbody.innerHTML = makeEmptyStateRow(colspan, title, hint, icon);
}


function renderActivityTimeline(issues = []) {

  const timelineEl = document.getElementById("activityTimeline");

  if (!timelineEl) return;



  const normalizedIssues = Array.isArray(issues) ? issues : [];

  const issueEvents = normalizedIssues.flatMap(issue => {

    const rollNo = String(issue.studrollNumber ?? issue.rollNumber ?? "-").trim() || "-";

    const bookId = String(issue.bookId ?? "-").trim() || "-";

    const base = [];



    const issueDateValue = String(issue.issueDate ?? issue.issuedDate ?? issue.dateIssued ?? issue.issue_date ?? "").trim();

    const returnDateValue = String(issue.returnDate ?? issue.returnedDate ?? issue.dateReturned ?? issue.return_date ?? "").trim();



    if (issueDateValue) {

      base.push({

        type: "issued",

        ts: Date.parse(issueDateValue),

        label: `Issued book ${bookId} to ${rollNo}`,

        time: issueDateValue

      });

    }



    if (isIssueReturned(issue) && returnDateValue) {

      const dueDateObj = parseDateOnly(issue.dueDate ?? issue.due_date ?? "");

      const returnDateObj = parseDateOnly(returnDateValue);

      const lateDays = dueDateObj && returnDateObj ? Math.max(0, dayDiff(dueDateObj, returnDateObj)) : 0;

      const isLate = lateDays > 0;

      base.push({

        type: isLate ? "fine" : "returned",

        ts: Date.parse(returnDateValue),

        label: isLate ? `Late return (${lateDays}d) for book ${bookId}` : `Returned book ${bookId} on time`,

        time: returnDateValue

      });

    }

    return base;

  });



  const notificationEvents = getNotifications().slice(0, 4).map((n, idx) => ({

    type: "note",

    ts: Date.parse(String(n.time || "")) || (Date.now() - idx * 1000),

    label: String(n.message || "Notification"),

    time: String(n.time || "-")

  }));



  const allEvents = [...issueEvents, ...notificationEvents]

    .sort((a, b) => (Number.isFinite(b.ts) ? b.ts : 0) - (Number.isFinite(a.ts) ? a.ts : 0))

    .slice(0, 8);



  if (!allEvents.length) {

    timelineEl.innerHTML = `<li>${makeEmptyState("No activity yet", "Issue or return actions will appear here.", "🕒", "Issue Book", "issueBook")}</li>`;

    return;

  }



  timelineEl.innerHTML = allEvents.map(item => `

    <li class="timeline-item ${item.type === "returned" ? "returned" : item.type === "fine" ? "fine" : ""}">

      <div>${escapeHtml(item.label)}</div>

      <div class="time">${escapeHtml(item.time)}</div>

    </li>

  `).join("");

}

  











async function addStudent(){

  const ok = await confirmAction("Add Student", "Do you want to save this student record?", "Add Student");

  if (!ok) return;



  const password = String(document.getElementById("sPass")?.value || "").trim();

  if (!password) {

    showErrorPopup("Password is required for student.");

    return;

  }



  const rawPhone = String(document.getElementById("sPhone")?.value || "").trim();

  const trimmedPhone = rawPhone.replace(/\\s+/g, "");

  if (trimmedPhone && !isValidPhoneNumber(trimmedPhone)) {

    showErrorPopup("Phone number must be exactly 10 digits.");

    return;

  }

  const studentEmail = String(document.getElementById("sEmail")?.value || "").trim();

  if (studentEmail && !isValidEmailAddress(studentEmail)) {

    showErrorPopup("Enter a valid email address.");

    return;

  }



  const student = {

    name: String(document.getElementById("sName")?.value || "").trim(),

    rollNumber: String(document.getElementById("sReg")?.value || "").trim(),

    department: String(document.getElementById("sDept")?.value || "").trim(),

    year: String(document.getElementById("sYear")?.value || "").trim(),

    password,

    phone: trimmedPhone,

    email: studentEmail

  };



  try {

    setGlobalLoading(true, "Adding student...");

    const res = await fetch(`${BASE_URL}/api/students/add`, {

      method: "POST",

      headers: { "Content-Type": "application/json" },

      body: JSON.stringify(student)

    });

    if (!res.ok) throw new Error(await res.text());

    await res.json();

    showSuccessPopup("Student added successfully.", true);

  } catch (err) {

    console.error(err);

    showErrorPopup("Failed to add student.");

  } finally {

    setGlobalLoading(false);

  }

}





async function addStaff(){

  const ok = await confirmAction("Add Staff", "Do you want to save this staff record?", "Add Staff");

  if (!ok) return;



  const rawStaffPhone = String(document.getElementById("staffPhone").value || "").trim();

  const trimmedStaffPhone = rawStaffPhone.replace(/\\s+/g, "");

  if (trimmedStaffPhone && !isValidPhoneNumber(trimmedStaffPhone)) {

    showErrorPopup("Phone number must be exactly 10 digits.");

    return;

  }

  const staffEmail = String(document.getElementById("staffEmail").value || "").trim();

  if (staffEmail && !isValidEmailAddress(staffEmail)) {

    showErrorPopup("Enter a valid email address.");
    
    return;

  }

  const staff = {

    staffCode: document.getElementById("staffCode").value,

    name: document.getElementById("staffName").value,

    department: document.getElementById("staffDept").value,

    email: staffEmail,

    phone: trimmedStaffPhone,

     staffType: document.getElementById("staffType").value,

    password: document.getElementById("staffPassword").value

  };



  try {

    setGlobalLoading(true, "Adding staff...");

    const res = await fetch(`${BASE_URL}/api/staff/add`, {

      method: "POST",

      headers: { "Content-Type": "application/json" },

      body: JSON.stringify(staff)

    });

    if (!res.ok) throw new Error(await res.text());

    await res.json();

    showSuccessPopup("Staff added successfully.", true);

  } catch (err) {

    console.error(err);

    showErrorPopup("Failed to add staff.");

  } finally {

    setGlobalLoading(false);

  }

}





async function addBook(){

  const ok = await confirmAction("Add Book", "Do you want to add this book to inventory?", "Add Book");

  if (!ok) return;



  const totalCopiesValue = document.getElementById("totalCopies").value;

  const book = {

    bookId: document.getElementById("bookId").value,

    title: document.getElementById("bookTitle").value,

    author: document.getElementById("bookAuthor").value,

    category: document.getElementById("bookCategory").value,

    publisher: document.getElementById("bookPublisher").value,

    rackNumber: document.getElementById("rackNumber").value,

    shelfNumber: document.getElementById("shelfNumber").value,

    Totalcopies: totalCopiesValue,

    Availablecopies: totalCopiesValue

  };



  try {

    setGlobalLoading(true, "Adding book...");

    const res = await fetch(`${BASE_URL}/api/books/add`, {

      method: "POST",

      headers: { "Content-Type": "application/json" },

      body: JSON.stringify(book)

    });

    if (!res.ok) throw new Error(await res.text());

    await res.json();

    showSuccessPopup("Book added successfully.", true);

  } catch (err) {

    console.error(err);

    showErrorPopup("Failed to add book.");

  } finally {

    setGlobalLoading(false);

  }

}



let allBooks = [];



function loadBooks() {

  console.log("loadBooks called");



  fetch(`${BASE_URL}/api/books/all`)

    .then(res => res.json())

    .then(data => {

      allBooks = data;

      renderBooks(allBooks);

    })

    .catch(err => console.error("Error fetching books:", err));

}



function getAvailableCopies(b) {

  if (b.Availablecopies != null) return b.Availablecopies;

  if (b.available != null) return b.available;

  if (b.copiesAvailable != null) return b.copiesAvailable;

  if (b.issuedCopies != null && b.Totalcopies != null) {

    return b.Totalcopies - b.issuedCopies;

  }

  return b.Totalcopies != null ? b.Totalcopies : "";

}



function renderBooks(books) {

  const table = document.getElementById("bookTableBody");

  if (!table) return;

  table.innerHTML = "";



  books.forEach(b => {

    table.innerHTML += `

      <tr>

        <td>${b.bookId || b.id || ""}</td>

        <td>${b.title || ""}</td>

        <td>${b.author || ""}</td>

        <td>${b.publisher || ""}</td>

        <td>${b.category || ""}</td>

        <td>${b.rackNumber || ""}</td>

        <td>${b.shelfNumber || ""}</td>

        <td>${b.Totalcopies != null ? b.Totalcopies : ""}</td>

        <td>${getAvailableCopies(b)}</td>

      </tr>

    `;

  });

}



function openBooks() {

  loadBooks();

}



let selectedIssueStudent = null;

let selectedIssueBook = null;

let returnIssueRows = [];

let returnSelectedStudent = null;

let pendingFineRows = [];

let selectedFineIssueId = null;

const MEMBER_TYPES = {

  STUDENTS: "STUDENTS",

  EMPLOYEES: "EMPLOYEES"

};

const sectionMemberTabs = {

  issueBook: MEMBER_TYPES.STUDENTS,

  returnBook: MEMBER_TYPES.STUDENTS,

  pendingBooks: MEMBER_TYPES.STUDENTS,

  returnedBooks: MEMBER_TYPES.STUDENTS,

  addUser: MEMBER_TYPES.STUDENTS

};

const RETURN_FINE_PER_DAY = 2;

const FINE_PAID_STORAGE_KEY = "lmsFinePaidIssueIds";

const FINE_PAYMENT_MAP_KEY = "lmsFinePaymentMap";



function getMemberTypeLabel(type) {

  return type === MEMBER_TYPES.EMPLOYEES ? "Employee" : "Student";

}



function getMemberIdFieldLabel(type) {

  return type === MEMBER_TYPES.EMPLOYEES ? "Employee ID" : "Roll Number";

}



function getBackendMemberType(type) {

  return type === MEMBER_TYPES.EMPLOYEES ? "EMPLOYEE" : "STUDENT";

}



function getMemberIdValue(member, type) {

  if (!member || typeof member !== "object") return "";

  if (type === MEMBER_TYPES.EMPLOYEES) {

    return String(member.staffCode ?? member.staffId ?? member.id ?? "").trim();

  }

  return String(member.rollNumber ?? member.studrollNumber ?? member.id ?? "").trim();

}



function setMemberInputPlaceholders() {

  const issueType = sectionMemberTabs.issueBook || MEMBER_TYPES.STUDENTS;

  const returnType = sectionMemberTabs.returnBook || MEMBER_TYPES.STUDENTS;

  const issueInput = document.getElementById("issueRollNo");

  const returnInput = document.getElementById("returnRollNo");

  if (issueInput) {

    issueInput.placeholder = issueType === MEMBER_TYPES.EMPLOYEES ? "Employee ID" : "Student Roll Number";

  }

  if (returnInput) {

    returnInput.placeholder = returnType === MEMBER_TYPES.EMPLOYEES ? "Employee ID" : "Student Roll Number";

  }

}



function setMemberTab(sectionId, memberType) {

  if (!sectionMemberTabs[sectionId]) return;

  const normalizedType = memberType === MEMBER_TYPES.EMPLOYEES ? MEMBER_TYPES.EMPLOYEES : MEMBER_TYPES.STUDENTS;

  sectionMemberTabs[sectionId] = normalizedType;



  document.querySelectorAll(`.member-tabs[data-tab-group="${sectionId}"] .member-tab`).forEach(btn => {

    btn.classList.toggle("active", String(btn.getAttribute("data-member-type") || "") === normalizedType);

  });

  setMemberInputPlaceholders();



  if (sectionId === "issueBook") {

    selectedIssueStudent = null;

    document.getElementById("issueStudentCard")?.classList.add("hidden");

    setIssueStatus("");

  } else if (sectionId === "returnBook") {

    resetReturnSectionState();

    setReturnStatus("");

  } else if (sectionId === "pendingBooks") {

    document.getElementById("issuedBooksTable")?.classList.toggle("hide-year", normalizedType === MEMBER_TYPES.EMPLOYEES);

    loadIssuedBooks();

  } else if (sectionId === "returnedBooks") {

    document.getElementById("returnedBooksTable")?.classList.toggle("hide-year", normalizedType === MEMBER_TYPES.EMPLOYEES);

    loadReturnedBooks();

  }

}



function setAddUserTab(memberType) {

  const normalizedType = memberType === MEMBER_TYPES.EMPLOYEES ? MEMBER_TYPES.EMPLOYEES : MEMBER_TYPES.STUDENTS;

  sectionMemberTabs.addUser = normalizedType;

  document.querySelectorAll(`.member-tabs[data-tab-group="addUser"] .member-tab`).forEach(btn => {

    btn.classList.toggle("active", String(btn.getAttribute("data-member-type") || "") === normalizedType);

  });

  document.getElementById("addUserStudentForm")?.classList.toggle("hidden", normalizedType !== MEMBER_TYPES.STUDENTS);

  document.getElementById("addUserStaffForm")?.classList.toggle("hidden", normalizedType !== MEMBER_TYPES.EMPLOYEES);

}



function setIssueStatus(message, isError = true) {

  const statusEl = document.getElementById("issueStatus");

  if (!statusEl) return;

  statusEl.style.color = isError ? "#b91c1c" : "#166534";

  statusEl.textContent = message || "";

}



function getFinePaymentMap() {

  try {

    const raw = localStorage.getItem(FINE_PAYMENT_MAP_KEY);

    const parsed = raw ? JSON.parse(raw) : {};

    return parsed && typeof parsed === "object" ? parsed : {};

  } catch (err) {

    return {};

  }

}



function getFinePaidIssueIds() {

  const map = getFinePaymentMap();

  const idsFromMap = Object.keys(map)

    .filter(id => map[id]?.paid === true)

    .map(id => Number(id))

    .filter(id => Number.isInteger(id));



  try {

    const raw = localStorage.getItem(FINE_PAID_STORAGE_KEY);

    const parsed = raw ? JSON.parse(raw) : [];

    const legacyIds = Array.isArray(parsed) ? parsed.map(v => Number(v)).filter(v => Number.isInteger(v)) : [];

    return [...new Set([...idsFromMap, ...legacyIds])];

  } catch (err) {

    return idsFromMap;

  }

}



function markFinePaidIssueIds(issueIds, method = "CASH") {

  const current = new Set(getFinePaidIssueIds());

  const paymentMap = getFinePaymentMap();

  issueIds.forEach(id => {

    const numericId = Number(id);

    if (!Number.isInteger(numericId)) return;

    current.add(numericId);

    paymentMap[String(numericId)] = {

      paid: true,

      method: method || "CASH",

      paidAt: new Date().toISOString()

    };

  });

  localStorage.setItem(FINE_PAID_STORAGE_KEY, JSON.stringify(Array.from(current)));

  localStorage.setItem(FINE_PAYMENT_MAP_KEY, JSON.stringify(paymentMap));

}



function isIssueReturned(issue) {

  const status = String(issue?.status ?? "").trim().toUpperCase();

  const returnDateRaw = String(issue?.returnDate ?? issue?.returnedDate ?? issue?.dateReturned ?? issue?.return_date ?? "").trim();

  const parsedReturnDate = Date.parse(returnDateRaw);

  const hasValidReturnDate = returnDateRaw && Number.isFinite(parsedReturnDate);

  return status === "RETURNED" || status === "RETURNED" || status === "RTN" || hasValidReturnDate;

}



function showIssueToast(message) {

  const toast = document.getElementById("issueToast");

  const text = document.getElementById("issueToastText");

  if (!toast || !text) return;

  text.textContent = message || "";

  toast.classList.remove("show");

  void toast.offsetWidth;

  toast.classList.add("show");

  setTimeout(() => {

    toast.classList.remove("show");

  }, 3200);

}



function hideIssueToast() {

  const toast = document.getElementById("issueToast");

  if (!toast) return;

  toast.classList.remove("show");

}



function setDefaultIssueDate() {

  const issueDate = document.getElementById("issueDate");

  if (!issueDate) return;

  if (!issueDate.value) {

    issueDate.value = new Date().toISOString().split("T")[0];

  }

}



function showIssueStudentCard(student) {

  const card = document.getElementById("issueStudentCard");

  if (!card) return;

  const memberType = student.__memberType || sectionMemberTabs.issueBook || MEMBER_TYPES.STUDENTS;

  document.getElementById("issueStudentIdText").textContent = `${getMemberIdFieldLabel(memberType)}: ${getMemberIdValue(student, memberType) || "-"}`;

  document.getElementById("issueStudentNameText").textContent = `Name: ${student.name || "-"}`;

  document.getElementById("issueStudentContactText").textContent = `Contact: ${student.email || student.phone || "-"}`;

  card.classList.remove("hidden");

}



function showIssueBookCard(book) {

  const card = document.getElementById("issueBookCard");

  if (!card) return;

  document.getElementById("issueBookIdText").textContent = `Book ID: ${book.bookId || book.id || "-"}`;

  document.getElementById("issueBookTitleText").textContent = `Title: ${book.title || "-"}`;

  document.getElementById("issueBookAuthorText").textContent = `Author: ${book.author || "-"}`;

  card.classList.remove("hidden");

}



async function fetchIssueStudent() {

  const rollNo = (document.getElementById("issueRollNo")?.value || "").trim();

  const memberType = sectionMemberTabs.issueBook || MEMBER_TYPES.STUDENTS;

  const memberLabel = getMemberTypeLabel(memberType);

  if (!rollNo) {

    setIssueStatus(`Enter ${memberLabel.toLowerCase()} ID.`);

    return;

  }



  try {

    setGlobalLoading(true, `Fetching ${memberLabel.toLowerCase()}...`);

    const endpoint = memberType === MEMBER_TYPES.EMPLOYEES ? `${BASE_URL}/api/staff/all` : `${BASE_URL}/api/students/getall`;

    const res = await fetch(endpoint);

    const members = await res.json();

    const student = Array.isArray(members)

      ? members.find(s => getMemberIdValue(s, memberType).toLowerCase() === rollNo.toLowerCase())

      : null;



    if (!student) {

      selectedIssueStudent = null;

      document.getElementById("issueStudentCard")?.classList.add("hidden");

      setIssueStatus(`${memberLabel} not found.`);

      showIssueToast(`${memberLabel} not found in database.`);

      return;

    }



    selectedIssueStudent = { ...student, __memberType: memberType };

    showIssueStudentCard(selectedIssueStudent);

    hideIssueToast();

    setIssueStatus("Member details loaded.", false);

  } catch (err) {

    console.error(err);

    setIssueStatus(`Failed to fetch ${memberLabel.toLowerCase()}.`);

  } finally {

    setGlobalLoading(false);

  }

}



async function fetchIssueBook() {

  const bookId = (document.getElementById("issueBookId")?.value || "").trim();

  if (!bookId) {

    setIssueStatus("Enter book ID.");

    return;

  }



  try {

    setGlobalLoading(true, "Fetching book...");

    const res = await fetch(`${BASE_URL}/api/books/all`);

    const books = await res.json();

    const book = Array.isArray(books)

      ? books.find(b => {

          const bookCode = String(b.bookId ?? "").trim().toLowerCase();

          const bookPk = String(b.id ?? "").trim().toLowerCase();

          const input = bookId.toLowerCase();

          return bookCode === input || bookPk === input;

        })

      : null;



    if (!book) {

      selectedIssueBook = null;

      document.getElementById("issueBookCard")?.classList.add("hidden");

      setIssueStatus("Book not found.");

      showIssueToast("Book not found in database.");

      return;

    }



    const available = Number(getAvailableCopies(book));

    if (!Number.isNaN(available) && available <= 0) {

      selectedIssueBook = null;

      document.getElementById("issueBookCard")?.classList.add("hidden");

      setIssueStatus("Book is not available for issuing.");

      showIssueToast("Book is not available for issue.");

      return;

    }



    selectedIssueBook = book;

    showIssueBookCard(book);

    hideIssueToast();

    setIssueStatus("Book details loaded.", false);

  } catch (err) {

    console.error(err);

    setIssueStatus("Failed to fetch book.");

  } finally {

    setGlobalLoading(false);

  }

}



async function issueBook() {

  if (!selectedIssueStudent || !selectedIssueBook) {

    setIssueStatus("Fetch student and book details before issuing.");

    return;

  }



  const issueDate = (document.getElementById("issueDate")?.value || "").trim();

  const dueDate = (document.getElementById("dueDate")?.value || "").trim();



  if (!issueDate || !dueDate) {

    setIssueStatus("Set issue date and return date.");

    return;

  }



  const memberType = selectedIssueStudent.__memberType || sectionMemberTabs.issueBook || MEMBER_TYPES.STUDENTS;

  const memberIdRaw = getMemberIdValue(selectedIssueStudent, memberType);

  const issueBookIdCandidatesRaw = [

    String(selectedIssueBook.bookId ?? "").trim(),

    String(selectedIssueBook.id ?? "").trim()

  ].filter(Boolean);



  if (!memberIdRaw) {

    setIssueStatus(`${getMemberIdFieldLabel(memberType)} is required.`);

    return;

  }



  const issueBookIdCandidates = [...new Set(issueBookIdCandidatesRaw.filter(v => /^\d+$/.test(v)).map(v => Number(v)))];



  if (issueBookIdCandidates.length === 0) {

    setIssueStatus("Book ID must be digits only to match backend Long fields.");

    return;

  }



  if (issueBookIdCandidates.some(v => !Number.isSafeInteger(v))) {

    setIssueStatus("Book ID is too large for frontend number handling.");

    return;

  }



  const confirmIssue = await confirmAction(

    "Issue Book",

    "Do you want to issue this book to the selected student?",

    "Issue"

  );

  if (!confirmIssue) return;



  try {

    setGlobalLoading(true, "Issuing book...");

    let issueOk = false;

    let lastError = "";



    for (const issueBookId of issueBookIdCandidates) {

      const params = new URLSearchParams({

        memberId: memberIdRaw,

        memberType: getBackendMemberType(memberType),

        bookId: String(issueBookId),

        issueDate,

        dueDate

      });

      const res = await fetch(`${BASE_URL}/api/issue/add?${params.toString()}`, {

        method: "POST"

      });



      if (res.ok) {

        issueOk = true;

        break;

      }



      lastError = await res.text();

    }



    if (!issueOk) {

      setIssueStatus(`Issue API failed. ${lastError || "Verify backend endpoint for issue book."}`);

      return;

    }



    setIssueStatus("Book issued successfully.", false);

    addNotification(`Book issued to member ${memberIdRaw}.`);

    document.getElementById("issueRollNo").value = "";

    document.getElementById("issueBookId").value = "";

    document.getElementById("dueDate").value = "";

    document.getElementById("issueStudentCard")?.classList.add("hidden");

    document.getElementById("issueBookCard")?.classList.add("hidden");

    selectedIssueStudent = null;

    selectedIssueBook = null;

    setDefaultIssueDate();

  } catch (err) {

    console.error(err);

    setIssueStatus("Failed to issue book.");

  } finally {

    setGlobalLoading(false);

  }

}



function setReturnStatus(message, isError = true) {

  const statusEl = document.getElementById("returnStatus");

  if (!statusEl) return;

  statusEl.style.color = isError ? "#b91c1c" : "#166534";

  statusEl.textContent = message || "";

}



function showReturnToast(isLate, returnDate, successCount, totalFine) {

  const toast = document.getElementById("returnToast");

  const title = document.getElementById("returnToastTitle");

  const text = document.getElementById("returnToastText");

  if (!toast || !title || !text) return;



  toast.classList.remove("good", "late", "show");

  toast.classList.add(isLate ? "late" : "good");



  if (isLate) {

    title.textContent = "😞 Returned Late";

    text.textContent = `${successCount} book(s) returned on ${returnDate}. Late fine: ${totalFine}.`;

  } else {

    title.textContent = "😊 Thank You For Returning";

    text.textContent = `${successCount} book(s) returned on ${returnDate}. Returned on time.`;

  }



  // restart animation

  void toast.offsetWidth;

  toast.classList.add("show");



  setTimeout(() => {

    toast.classList.remove("show");

  }, 4200);

}



function setDefaultReturnDate() {

  const returnDateInput = document.getElementById("actualReturnDate");

  if (!returnDateInput) return;

  if (!returnDateInput.value) {

    returnDateInput.value = new Date().toISOString().split("T")[0];

  }

  recalculateReturnFineAndNotes();

}



function showReturnStudentCard(student, rollNo) {

  const card = document.getElementById("returnStudentCard");

  if (!card) return;

  const memberType = student.__memberType || sectionMemberTabs.returnBook || MEMBER_TYPES.STUDENTS;

  const normalizedRollNo = rollNo || getMemberIdValue(student, memberType) || "-";

  const year = memberType === MEMBER_TYPES.EMPLOYEES

    ? "-"

    : (student.year || student.Year || student.studentYear || "-");

  const dept = student.department || student.dept || student.branch || "-";

  document.getElementById("returnStudentIdText").textContent = `${getMemberIdFieldLabel(memberType)}: ${normalizedRollNo}`;

  document.getElementById("returnStudentNameText").textContent = `Name: ${student.name || "-"}`;

  const yearDept = document.getElementById("returnStudentYearDeptText");

  if (memberType === MEMBER_TYPES.EMPLOYEES) {

    if (yearDept) yearDept.style.display = "none";

  } else if (yearDept) {

    yearDept.style.display = "";

    yearDept.textContent = `Year / Department: ${year} / ${dept}`;

  }

  card.classList.remove("hidden");

}



function resetReturnSectionState() {

  returnIssueRows = [];

  returnSelectedStudent = null;

  const tbody = document.getElementById("returnIssueTableBody");

  if (tbody) {

    tbody.innerHTML = `<tr><td colspan="5">Search a member to view issued books.</td></tr>`;

  }

  const selectAll = document.getElementById("returnSelectAll");

  if (selectAll) selectAll.checked = false;

  document.getElementById("returnStudentCard")?.classList.add("hidden");

  const info = document.getElementById("returnSelectionInfo");

  if (info) info.textContent = "";

  const notesEl = document.getElementById("returnNotes");

  const fineEl = document.getElementById("returnFineAmount");

  if (notesEl) notesEl.value = "";

  if (fineEl) fineEl.value = "0";

}



function getSelectedReturnIssueIds() {

  const checkboxes = document.querySelectorAll(".return-issue-checkbox:checked");

  return Array.from(checkboxes)

    .map(cb => Number(cb.getAttribute("data-issue-id")))

    .filter(id => Number.isInteger(id));

}



function updateReturnSelectionInfo() {

  const selectedIds = new Set(getSelectedReturnIssueIds());

  const selectedRows = returnIssueRows.filter(row => selectedIds.has(Number(row.issueId)));

  const info = document.getElementById("returnSelectionInfo");

  if (!info) return;

  if (!selectedRows.length) {

    info.textContent = "No books selected.";

    recalculateReturnFineAndNotes();

    return;

  }

  const titles = selectedRows.map(row => row.title || `Book ${row.bookId}`).join(", ");

  info.textContent = `${selectedRows.length} selected: ${titles}`;

  recalculateReturnFineAndNotes();

}



function parseDateOnly(value) {

  if (!value) return null;

  const text = String(value).trim();

  if (!text) return null;



  if (/^\d{4}-\d{2}-\d{2}$/.test(text)) {

    const date = new Date(`${text}T00:00:00`);

    return Number.isNaN(date.getTime()) ? null : date;

  }



  const slash = text.match(/^(\d{1,2})[\/-](\d{1,2})[\/-](\d{4})$/);

  if (slash) {

    const day = Number(slash[1]);

    const month = Number(slash[2]);

    const year = Number(slash[3]);

    const date = new Date(year, month - 1, day);

    return Number.isNaN(date.getTime()) ? null : date;

  }



  const parsed = new Date(text);

  return Number.isNaN(parsed.getTime()) ? null : parsed;

}



function dayDiff(fromDate, toDate) {

  const msPerDay = 24 * 60 * 60 * 1000;

  const start = new Date(fromDate.getFullYear(), fromDate.getMonth(), fromDate.getDate());

  const end = new Date(toDate.getFullYear(), toDate.getMonth(), toDate.getDate());

  return Math.floor((end - start) / msPerDay);

}



function recalculateReturnFineAndNotes() {

  const notesEl = document.getElementById("returnNotes");

  const fineEl = document.getElementById("returnFineAmount");

  const returnDateValue = (document.getElementById("actualReturnDate")?.value || "").trim();

  if (!notesEl || !fineEl) return;



  const selectedIds = new Set(getSelectedReturnIssueIds());

  const selectedRows = returnIssueRows.filter(row => selectedIds.has(Number(row.issueId)));



  if (!selectedRows.length) {

    notesEl.value = "";

    fineEl.value = "0";

    return;

  }



  const returnDate = parseDateOnly(returnDateValue);

  if (!returnDate) {

    notesEl.value = "Set return date to calculate late days.";

    fineEl.value = "0";

    return;

  }



  let totalFine = 0;

  const lateLines = [];



  selectedRows.forEach(row => {

    const dueDate = parseDateOnly(row.dueDate);

    if (!dueDate) return;

    const lateDays = Math.max(0, dayDiff(dueDate, returnDate));

    if (lateDays > 0) {

      const rowFine = lateDays * RETURN_FINE_PER_DAY;

      totalFine += rowFine;

      lateLines.push(`${row.title || row.bookId}: ${lateDays} day(s) late, fine ${rowFine}`);

    }

  });



  if (!lateLines.length) {

    notesEl.value = `No late return. ${selectedRows.length} selected book(s) on time.`;

    fineEl.value = "0";

    return;

  }



  notesEl.value = `${lateLines.join("; ")}. Total fine: ${totalFine}`;

  fineEl.value = String(totalFine);

}



function toggleSelectAllReturnIssues(checked) {

  document.querySelectorAll(".return-issue-checkbox").forEach(cb => {

    cb.checked = checked;

  });

  updateReturnSelectionInfo();

}



function onReturnIssueCheckboxChange() {

  const all = Array.from(document.querySelectorAll(".return-issue-checkbox"));

  const checked = all.filter(cb => cb.checked);

  const selectAll = document.getElementById("returnSelectAll");

  if (selectAll) {

    selectAll.checked = all.length > 0 && checked.length === all.length;

  }

  updateReturnSelectionInfo();

}



async function fetchReturnStudentBooks(options = {}) {

  const silentNoActive = Boolean(options.silentNoActive);

  const rollNoRaw = (document.getElementById("returnRollNo")?.value || "").trim();

  const memberType = sectionMemberTabs.returnBook || MEMBER_TYPES.STUDENTS;

  const memberLabel = getMemberTypeLabel(memberType);

  if (!rollNoRaw) {

    setReturnStatus(`Enter ${memberLabel.toLowerCase()} ID.`);

    return;

  }

  const rollNo = memberType === MEMBER_TYPES.EMPLOYEES ? rollNoRaw.toUpperCase() : rollNoRaw;



  setReturnStatus("");

  const tbody = document.getElementById("returnIssueTableBody");

  if (!tbody) return;

  tbody.innerHTML = `<tr><td colspan="5">Loading...</td></tr>`;



  try {

    setGlobalLoading(true, "Fetching issued books...");

    const memberEndpoint = memberType === MEMBER_TYPES.EMPLOYEES ? `${BASE_URL}/api/staff/all` : `${BASE_URL}/api/students/getall`;

    const backendType = memberType === MEMBER_TYPES.EMPLOYEES ? "EMPLOYEE" : "STUDENT";

    const [studentRes, issueRes, booksRes] = await Promise.all([

      fetch(memberEndpoint),

      fetch(`${BASE_URL}/api/issue/member/${encodeURIComponent(backendType)}/${encodeURIComponent(rollNo)}`),

      fetch(`${BASE_URL}/api/books/all`)

    ]);



    const students = studentRes.ok ? await studentRes.json() : [];

    const issues = issueRes.ok ? await issueRes.json() : [];

    const books = booksRes.ok ? await booksRes.json() : [];



    const student = Array.isArray(students)

      ? students.find(s => {

          const id = getMemberIdValue(s, memberType);

          const cmp = memberType === MEMBER_TYPES.EMPLOYEES ? id.toUpperCase() : id.toLowerCase();

          return cmp === (memberType === MEMBER_TYPES.EMPLOYEES ? rollNo.toUpperCase() : rollNo.toLowerCase());

        })

      : null;



    returnSelectedStudent = student ? { ...student, __memberType: memberType } : { rollNumber: rollNo, name: "-", __memberType: memberType };

    showReturnStudentCard(returnSelectedStudent, rollNo);



    const activeIssues = Array.isArray(issues) ? issues.filter(i => !isIssueReturned(i)) : [];

    if (!activeIssues.length) {

      returnIssueRows = [];

      tbody.innerHTML = silentNoActive

        ? `<tr><td colspan="5">All selected books returned successfully.</td></tr>`

        : `<tr><td colspan="5">No active issued books for this ${memberLabel.toLowerCase()}.</td></tr>`;

      if (!silentNoActive) {

        setReturnStatus(`No active books to return for this ${memberLabel.toLowerCase()}.`);

      }

      updateReturnSelectionInfo();

      return;

    }



    const bookMap = new Map();

    if (Array.isArray(books)) {

      books.forEach(b => {

        const keys = [String(b.bookId ?? "").trim(), String(b.id ?? "").trim()].filter(Boolean);

        keys.forEach(key => bookMap.set(key, b));

      });

    }



    returnIssueRows = activeIssues

      .map(issue => {

        const issueId = Number(issue.issueId ?? issue.id);

        const issueBookId = String(issue.bookId ?? "").trim();

        const book = bookMap.get(issueBookId) || {};

        return {

          issueId,

          bookId: issueBookId || "-",

          title: book.title || issue.bookName || "-",

          author: book.author || issue.author || "-",

          dueDate: issue.dueDate || "-"

        };

      })

      .filter(row => Number.isInteger(row.issueId));



    if (!returnIssueRows.length) {

      tbody.innerHTML = `<tr><td colspan="5">Issue IDs not found in response.</td></tr>`;

      setReturnStatus("Cannot return books: issue ID missing from backend response.");

      return;

    }



    tbody.innerHTML = returnIssueRows.map(row => `

      <tr>

        <td><input type="checkbox" class="return-issue-checkbox" data-issue-id="${row.issueId}" onchange="onReturnIssueCheckboxChange()"></td>

        <td>${escapeHtml(row.bookId)}</td>

        <td>${escapeHtml(row.title)}</td>

        <td>${escapeHtml(row.author)}</td>

        <td>${escapeHtml(row.dueDate)}</td>

      </tr>

    `).join("");



    const selectAll = document.getElementById("returnSelectAll");

    if (selectAll) selectAll.checked = false;

    updateReturnSelectionInfo();

    recalculateReturnFineAndNotes();

    setReturnStatus("Issued books loaded.", false);

  } catch (err) {

    console.error(err);

    tbody.innerHTML = `<tr><td colspan="5">Failed to fetch issued books.</td></tr>`;

    setReturnStatus(`Failed to fetch ${memberLabel.toLowerCase()} issued books.`);

  } finally {

    setGlobalLoading(false);

  }

}



async function returnSelectedBooks() {

  const selectedIds = getSelectedReturnIssueIds();

  if (!selectedIds.length) {

    setReturnStatus("Select at least one book to return.");

    return;

  }



  const returnDate = (document.getElementById("actualReturnDate")?.value || "").trim();

  if (!returnDate) {

    setReturnStatus("Set a return date.");

    return;

  }

  const returnDateObj = parseDateOnly(returnDate);

  if (!returnDateObj) {

    setReturnStatus("Invalid return date.");

    return;

  }

  const totalFine = Number(document.getElementById("returnFineAmount")?.value || "0");

  const isLateReturn = Number.isFinite(totalFine) && totalFine > 0;

  const confirmReturn = await confirmAction(

    "Return Books",

    isLateReturn

      ? `Selected books have fine ${totalFine}. Continue return process?`

      : "Do you want to return selected books now?",

    "Return"

  );

  if (!confirmReturn) return;

  setGlobalLoading(true, "Processing return...");

  try {

    let successCount = 0;

    const failures = [];



    for (const issueId of selectedIds) {

      try {

        const issueRow = returnIssueRows.find(row => Number(row.issueId) === Number(issueId));

        const dueDateObj = parseDateOnly(issueRow?.dueDate);

        const perIssueLateDays = dueDateObj ? Math.max(0, dayDiff(dueDateObj, returnDateObj)) : 0;

        const perIssueFine = perIssueLateDays * RETURN_FINE_PER_DAY;

        const params = new URLSearchParams({

          issueId: String(issueId),

          fineAmount: String(perIssueFine)

        });

        const res = await fetch(`${BASE_URL}/api/issue/return?${params.toString()}`, { method: "POST" });

        if (res.ok) {

          successCount += 1;

        } else {

          const errorText = await res.text();

          failures.push(`Issue ${issueId}: ${errorText || "failed"}`);

        }

      } catch (err) {

        failures.push(`Issue ${issueId}: ${err?.message || "failed"}`);

      }

    }



    if (successCount > 0 && failures.length === 0) {

      setReturnStatus(`${successCount} book(s) returned successfully.`, false);

    } else if (successCount > 0) {

      setReturnStatus(`${successCount} returned, ${failures.length} failed. ${failures[0]}`);

    } else {

      setReturnStatus(`Return failed. ${failures[0] || ""}`);

    }



    if (successCount > 0) {

      addNotification(`${successCount} book(s) returned successfully.`);

      showReturnToast(isLateReturn, returnDate, successCount, Number.isFinite(totalFine) ? totalFine : 0);

      const notesEl = document.getElementById("returnNotes");

      const fineEl = document.getElementById("returnFineAmount");

      if (notesEl) notesEl.value = "";

      if (fineEl) fineEl.value = "0";

      await fetchReturnStudentBooks({ silentNoActive: true });

      loadIssuedBooks();

      loadReturnedBooks();

      loadPendingFines();

    }

  } finally {

    setGlobalLoading(false);

  }

}









let cachedStudents = [];

let cachedStaff = [];

let activeUserView = 'students';



function renderUsersTable() {

  const body = document.getElementById("usersTableBody");

  const headerPrimary = document.getElementById("usersHeaderPrimary");

  const headerSecondary = document.getElementById("usersHeaderSecondary");

  if (headerPrimary) {

    headerPrimary.textContent = activeUserView === "students" ? "Reg No" : "Staff Code";

  }

  if (headerSecondary) {

    headerSecondary.textContent = activeUserView === "students" ? "Year" : "Staff Type";

  }

  if (!body) return;



  const dataset = activeUserView === "students" ? cachedStudents : cachedStaff;

  const label = activeUserView === "students" ? "students" : "employees";



  if (!dataset.length) {

    body.innerHTML = `<tr><td colspan="6">No ${label} found.</td></tr>`;

    return;

  }



  body.innerHTML = dataset.map(item => {

    if (activeUserView === "students") {

      return `

        <tr>

          <td>${escapeHtml(item.rollNumber ?? item.studentId ?? "-")}</td>

          <td>${escapeHtml(item.name ?? "-")}</td>

          <td>${escapeHtml(item.department ?? "-")}</td>

          <td>${escapeHtml(item.year ?? "-")}</td>

          <td>${escapeHtml(item.email ?? "-")}</td>

          <td>${escapeHtml(item.phone ?? "-")}</td>

        </tr>

      `;

    }

    return `

      <tr>

        <td>${escapeHtml(item.staffCode ?? item.staffId ?? "-")}</td>

        <td>${escapeHtml(item.name ?? "-")}</td>

        <td>${escapeHtml(item.department ?? "-")}</td>

        <td>${escapeHtml(item.staffType ?? "-")}</td>

        <td>${escapeHtml(item.email ?? "-")}</td>

        <td>${escapeHtml(item.phone ?? "-")}</td>

      </tr>

    `;

  }).join("");

}



async function fetchStudents() {

  try {

    const res = await fetch(`${BASE_URL}/api/students/getall`);

    const students = res.ok ? await res.json() : [];

    cachedStudents = Array.isArray(students) ? students : [];

  } catch (err) {

    console.error("Failed to load students:", err);

    cachedStudents = [];

  } finally {

    if (activeUserView === "students") {

      renderUsersTable();

    }

  }

}



async function fetchStaff() {

  try {

    const res = await fetch(`${BASE_URL}/api/staff/all`);

    const staffs = res.ok ? await res.json() : [];

    cachedStaff = Array.isArray(staffs) ? staffs : [];

  } catch (err) {

    console.error("Failed to load staff:", err);

    cachedStaff = [];

  } finally {

    if (activeUserView === "staffs") {

      renderUsersTable();

    }

  }

}



function setUserView(type) {

  activeUserView = type;

  document.querySelectorAll(".user-type-btn").forEach(btn => {

    btn.classList.toggle("active", btn.dataset.type === type);

  });

  renderUsersTable();

  if (type === "students" && !cachedStudents.length) {

    fetchStudents();

  } else if (type === "staffs" && !cachedStaff.length) {

    fetchStaff();

  }

}



function escapeHtml(value) {

  return String(value ?? "")

    .replace(/&/g, "&amp;")

    .replace(/</g, "&lt;")

    .replace(/>/g, "&gt;")

    .replace(/\"/g, "&quot;")

    .replace(/'/g, "&#39;");

}





async function fetchAllIssues() {

  const endpoints = [

    `${BASE_URL}/api/issue/all`,

    `${BASE_URL}/api/issue/getall`,

    `${BASE_URL}/api/issues/all`

  ];



  let lastError = "";

  for (const endpoint of endpoints) {

    try {

      const res = await fetch(endpoint);

      if (res.ok) {

        const data = await res.json();

        if (Array.isArray(data)) return data;

        if (data && typeof data === "object") {

          if (Array.isArray(data.issues)) return data.issues;

          if (Array.isArray(data.data)) return data.data;

          if (Array.isArray(data.results)) return data.results;

        }

        lastError = "Issues API response is not in expected array format.";

        continue;

      }

      lastError = await res.text();

    } catch (err) {

      lastError = err?.message || "Failed to fetch issues.";

    }

  }

  throw new Error(lastError || "Unable to fetch issued books.");

}



function openSectionById(sectionId) {

  const menuItem = Array.from(document.querySelectorAll(".menu li"))

    .find(li => String(li.getAttribute("onclick") || "").includes(`'${sectionId}'`));

  if (!menuItem) return;

  showSection(sectionId, menuItem);

}



function isCompactLayout() {

  return window.matchMedia("(max-width: 1100px)").matches;

}



function toggleSidebar(forceOpen) {

  if (!isCompactLayout()) {

    document.body.classList.remove("sidebar-open");

    return;

  }

  const openNow = document.body.classList.contains("sidebar-open");

  const nextOpen = typeof forceOpen === "boolean" ? forceOpen : !openNow;

  document.body.classList.toggle("sidebar-open", nextOpen);

}



function closeSidebarOnCompact() {

  if (isCompactLayout()) {

    document.body.classList.remove("sidebar-open");

  }

}



async function safeFetchArray(url) {

  try {

    const res = await fetch(url);

    if (!res.ok) return [];

    const data = await res.json();

    return Array.isArray(data) ? data : [];

  } catch (err) {

    return [];

  }

}



async function loadDashboardOverview() {

  if (isDashboardOverviewLoading) {
    return;
  }
  isDashboardOverviewLoading = true;

  function showOverviewError(message) {
    const totalBooksEl = document.getElementById("totalBooks");
    const totalStudentsEl = document.getElementById("totalStudents");
    const totalStaffEl = document.getElementById("totalStaff");
    const pendingFinesEl = document.getElementById("pendingFinesCount");
    const recentBody = document.getElementById("recentBorrowersBody");
    const issuedValueEl = document.getElementById("dashIssuedValue");
    const availableValueEl = document.getElementById("dashAvailableValue");
    const issuedBarEl = document.getElementById("dashIssuedBar");
    const availableBarEl = document.getElementById("dashAvailableBar");

    if (totalBooksEl) totalBooksEl.textContent = "0";
    if (totalStudentsEl) totalStudentsEl.textContent = "0";
    if (totalStaffEl) totalStaffEl.textContent = "0";
    if (pendingFinesEl) pendingFinesEl.textContent = "0";
    if (issuedValueEl) issuedValueEl.textContent = "0";
    if (availableValueEl) availableValueEl.textContent = "0";
    if (issuedBarEl) issuedBarEl.style.width = "0%";
    if (availableBarEl) availableBarEl.style.width = "0%";

    if (recentBody) {
      recentBody.innerHTML = makeEmptyStateRow(4, "Dashboard failed", message, "⚠️", "Retry", "dashboard");
    }

    const timelineEl = document.getElementById("activityTimeline");
    if (timelineEl) {
      timelineEl.innerHTML = `<li class="timeline-item">${makeEmptyState("Timeline error", message, "⚠️", "Refresh", "dashboard")}</li>`;
    }
  }

  const totalBooksEl = document.getElementById("totalBooks");

  const totalStudentsEl = document.getElementById("totalStudents");

  const totalStaffEl = document.getElementById("totalStaff");

  const pendingFinesEl = document.getElementById("pendingFinesCount");

  const recentBody = document.getElementById("recentBorrowersBody");

  const issuedValueEl = document.getElementById("dashIssuedValue");

  const availableValueEl = document.getElementById("dashAvailableValue");

  const issuedBarEl = document.getElementById("dashIssuedBar");

  const availableBarEl = document.getElementById("dashAvailableBar");



  if (!totalBooksEl || !recentBody) return;



  recentBody.innerHTML = makeEmptyStateRow(4, "Loading dashboard", "Fetching latest borrowers and counters.", "⏳");



  let books = [], students = [], staffs = [], issues = [];

  try {
    [books, students, staffs, issues] = await Promise.all([
      safeFetchArray(`${BASE_URL}/api/books/all`),
      safeFetchArray(`${BASE_URL}/api/students/getall`),
      safeFetchArray(`${BASE_URL}/api/staff/all`),
      fetchAllIssues().catch(() => [])
    ]);
  } catch (err) {
    console.error("loadDashboardOverview error", err);
    showOverviewError("Unable to load dashboard data. Please check your network and retry.");
    return;
  } finally {
    isDashboardOverviewLoading = false;
  }



  animateCounter(totalBooksEl, books.length);

  if (totalStudentsEl) animateCounter(totalStudentsEl, students.length);

  if (totalStaffEl) animateCounter(totalStaffEl, staffs.length);



  const issuedActive = (issues || []).filter(i => !isIssueReturned(i));

  const totalAvailable = books.reduce((acc, b) => {

    const count = Number(getAvailableCopies(b));

    return acc + (Number.isNaN(count) ? 0 : count);

  }, 0);

  const totalIssued = issuedActive.length;

  const barsTotal = Math.max(1, totalAvailable + totalIssued);



  if (issuedValueEl) animateCounter(issuedValueEl, totalIssued);

  if (availableValueEl) animateCounter(availableValueEl, totalAvailable);

  if (issuedBarEl) issuedBarEl.style.width = `${Math.min(100, (totalIssued / barsTotal) * 100)}%`;

  if (availableBarEl) availableBarEl.style.width = `${Math.min(100, (totalAvailable / barsTotal) * 100)}%`;



  const pendingFineCount = (issues || []).filter(issue => {

    if (!isIssueReturned(issue)) return false;

    const dueDateObj = parseDateOnly(issue.dueDate);

    const returnDateObj = parseDateOnly(issue.returnDate);

    if (!dueDateObj || !returnDateObj) return false;

    const lateDays = Math.max(0, dayDiff(dueDateObj, returnDateObj));

    if (lateDays <= 0) return false;

    const fineAmount = Number.isFinite(Number(issue.fineAmount)) ? Number(issue.fineAmount) : lateDays * RETURN_FINE_PER_DAY;

    return getIssuePaymentStatus(issue, Number(issue.issueId ?? issue.id), fineAmount) !== "FINE_PAID";

  }).length;

  if (pendingFinesEl) animateCounter(pendingFinesEl, pendingFineCount);

  renderActivityTimeline(issues);



  const studentMap = new Map(students.map(s => [String(s.rollNumber ?? s.studrollNumber ?? "").trim().toLowerCase(), s]));

  const staffMap = new Map(staffs.map(s => [String(s.staffCode ?? s.staffId ?? s.id ?? "").trim().toLowerCase(), s]));

  const bookMap = new Map();

  books.forEach(b => {

    [String(b.bookId ?? "").trim(), String(b.id ?? "").trim()].filter(Boolean).forEach(key => bookMap.set(key, b));

  });



  const recentRows = (issues || [])

    .slice()

    .sort((a, b) => String(b.issueDate ?? "").localeCompare(String(a.issueDate ?? "")))

    .slice(0, 6);



  if (!recentRows.length) {

    recentBody.innerHTML = makeEmptyStateRow(4, "No recent borrowers", "Issue a book to start tracking activity.", "📚", "Issue Book", "issueBook");

    return;

  }



  recentBody.innerHTML = recentRows.map(issue => {

    const memberId = String(issue.memberId ?? issue.studrollNumber ?? issue.rollNumber ?? "").trim();

    const issueBookId = String(issue.bookId ?? "").trim();

    const issueMemberTypeRaw = String(issue.memberType ?? "").trim().toUpperCase();

    const rowMemberType = issueMemberTypeRaw === "EMPLOYEE" || issueMemberTypeRaw === "EMPLOYEES"

      ? MEMBER_TYPES.EMPLOYEES

      : MEMBER_TYPES.STUDENTS;

    const student = memberId ? studentMap.get(memberId.toLowerCase()) : null;

    const staff = memberId ? staffMap.get(memberId.toLowerCase()) : null;

    const member = rowMemberType === MEMBER_TYPES.EMPLOYEES ? staff : (student || {});

    const book = bookMap.get(issueBookId) || {};



    return `

      <tr>

        <td>${escapeHtml(memberId || "-")}</td>

        <td>${escapeHtml(member?.name || issue.studentName || "-")}</td>

        <td>${escapeHtml(book.title || issue.bookName || "-")}</td>

        <td>${escapeHtml(issue.issueDate || "-")}</td>

      </tr>

    `;

  }).join("");

}



async function loadIssuedBooks() {

  const tbody = document.getElementById("issuedBooksTableBody");

  if (!tbody) return;

  const memberTypeFilter = sectionMemberTabs.pendingBooks || MEMBER_TYPES.STUDENTS;



  tbody.innerHTML = `<tr><td colspan="10"><span class="loader-spinner" style="display:inline-block;vertical-align:middle;margin-right:8px;"></span>Loading issued books...</td></tr>`;



  try {

    const [issues, students, staffs, books] = await Promise.all([

      fetchAllIssues(),

      fetch(`${BASE_URL}/api/students/getall`).then(r => (r.ok ? r.json() : [])),

      fetch(`${BASE_URL}/api/staff/all`).then(r => (r.ok ? r.json() : [])),

      fetch(`${BASE_URL}/api/books/all`).then(r => (r.ok ? r.json() : []))

    ]);



    const activeIssues = Array.isArray(issues) ? issues.filter(i => !isIssueReturned(i)) : [];

    if (!activeIssues.length) {

      tbody.innerHTML = makeEmptyStateRow(10, "No active issued books", "All books are returned or none issued yet.", "?", "Issue Book", "issueBook");

      return;

    }



    const studentMap = new Map();

    if (Array.isArray(students)) {

      students.forEach(s => {

        const key = String(s.rollNumber ?? s.studrollNumber ?? "").trim().toLowerCase();

        if (key) studentMap.set(key, s);

      });

    }



    const staffMap = new Map();

    if (Array.isArray(staffs)) {

      staffs.forEach(s => {

        const key = String(s.staffCode ?? s.staffId ?? s.id ?? "").trim().toLowerCase();

        if (key) staffMap.set(key, s);

      });

    }



    const bookMap = new Map();

    if (Array.isArray(books)) {

      books.forEach(b => {

        const keys = [String(b.bookId ?? "").trim(), String(b.id ?? "").trim()].filter(Boolean);

        keys.forEach(key => bookMap.set(key, b));

      });

    }



    const rowsHtml = activeIssues.map(issue => {

      const memberId = String(issue.memberId ?? issue.studrollNumber ?? issue.rollNumber ?? "").trim();

      const issueBookId = String(issue.bookId ?? issue.id ?? "").trim();

      const student = memberId ? studentMap.get(memberId.toLowerCase()) : null;

      const staff = memberId ? staffMap.get(memberId.toLowerCase()) : null;

      const issueMemberTypeRaw = String(issue.memberType ?? "").trim().toUpperCase();

      const rowMemberType = issueMemberTypeRaw === "EMPLOYEE" || issueMemberTypeRaw === "EMPLOYEES"

        ? MEMBER_TYPES.EMPLOYEES

        : (staff ? MEMBER_TYPES.EMPLOYEES : MEMBER_TYPES.STUDENTS);

      if (memberTypeFilter !== rowMemberType) return null;

      const member = rowMemberType === MEMBER_TYPES.EMPLOYEES ? staff : (student || {});

      const book = bookMap.get(issueBookId) || {};



      const memberName = member.name || issue.studentName || "-";

      const memberYearOrType = rowMemberType === MEMBER_TYPES.EMPLOYEES

        ? (member.staffType || member.role || "-")

        : (member.year || member.Year || member.studentYear || issue.year || issue.studentYear || "-");

      const memberDept = member.department || member.dept || member.branch || issue.department || issue.dept || "-";

      const bookName = book.title || issue.bookName || "-";

      const bookAuthor = book.author || issue.author || "-";

      const bookCategory = book.category || issue.category || "-";

      const issuedOn = issue.issueDate || "-";

      const returnOn = issue.dueDate || issue.returnDate || "-";

      const dueDateObj = parseDateOnly(issue.dueDate);

      const today = new Date();

      const isOverdue = dueDateObj ? dayDiff(dueDateObj, today) > 0 : false;

      const statusKey = isOverdue ? "OVERDUE" : "ACTIVE";

      const statusBadge = isOverdue

        ? makeStatusBadge("overdue", "OVERDUE")

        : makeStatusBadge("paid", "ACTIVE");



      return `

        <tr class="member-row" data-status="${statusKey}" data-member-type="${rowMemberType}">

          <td>${escapeHtml(memberName)}</td>

          <td>${escapeHtml(memberId || "-")}</td>

          <td class="member-year-col">${escapeHtml(memberYearOrType)}</td>

          <td class="member-dept-col">${escapeHtml(memberDept)}</td>

          <td>${escapeHtml(bookName)}</td>

          <td>${escapeHtml(bookAuthor)}</td>

          <td>${escapeHtml(bookCategory)}</td>

          <td>${escapeHtml(issuedOn)}</td>

          <td>${escapeHtml(returnOn)}</td>

          <td>${statusBadge}</td>

        </tr>

      `;

    }).filter(Boolean).join("");



    tbody.innerHTML = rowsHtml || makeEmptyStateRow(

      10,

      `No active issued books for ${memberTypeFilter === MEMBER_TYPES.EMPLOYEES ? "employees" : "students"}.`,

      "Switch tab or issue a new book.",

      "Books",

      "Issue Book",

      "issueBook"

    );

    filterIssuedBooksTable();

  } catch (err) {

    console.error(err);

    setTableLoadError(tbody, 10, "Failed to load issued books", "Check backend issue API and try again.");
    addNotification("Issued books load failed. Please check server logs and retry.", "error");

  }

}

function getIssuePaymentStatus(issue, issueId, fineAmount) {

  if (fineAmount <= 0) return "ON_TIME";



  const paymentStatus = String(issue?.paymentStatus ?? issue?.fineStatus ?? "").trim().toUpperCase();

  const paidFlags = [

    issue?.finePaid === true,

    issue?.paid === true,

    paymentStatus === "PAID",

    paymentStatus === "FINE_PAID",

    String(issue?.status ?? "").trim().toUpperCase() === "FINE_PAID"

  ];

  const paidFromBackend = paidFlags.some(Boolean);

  const paidFromLocal = getFinePaidIssueIds().includes(Number(issueId));

  return paidFromBackend || paidFromLocal ? "FINE_PAID" : "LATE_UNPAID";

}



function getIssuePaymentMethod(issue, issueId) {

  const backendMethod = String(issue?.paymentMethod ?? issue?.finePaymentMethod ?? "").trim().toUpperCase();

  if (backendMethod) return backendMethod;

  const map = getFinePaymentMap();

  const localMethod = String(map[String(issueId)]?.method ?? "").trim().toUpperCase();

  return localMethod || "-";

}



async function loadReturnedBooks() {

  const tbody = document.getElementById("returnedBooksTableBody");

  if (!tbody) return;

  const memberTypeFilter = sectionMemberTabs.returnedBooks || MEMBER_TYPES.STUDENTS;



  tbody.innerHTML = `<tr><td colspan="12"><span class="loader-spinner" style="display:inline-block;vertical-align:middle;margin-right:8px;"></span>Loading returned books...</td></tr>`;



  try {

    const [issues, students, staffs, books] = await Promise.all([

      fetchAllIssues(),

      fetch(`${BASE_URL}/api/students/getall`).then(r => (r.ok ? r.json() : [])),

      fetch(`${BASE_URL}/api/staff/all`).then(r => (r.ok ? r.json() : [])),

      fetch(`${BASE_URL}/api/books/all`).then(r => (r.ok ? r.json() : []))

    ]);



    const returnedIssues = Array.isArray(issues) ? issues.filter(isIssueReturned) : [];

    if (!returnedIssues.length) {

      tbody.innerHTML = makeEmptyStateRow(12, "No returned books found", "Returned records will show here.", "Books");

      return;

    }



    const studentMap = new Map();

    if (Array.isArray(students)) {

      students.forEach(s => {

        const key = String(s.rollNumber ?? s.studrollNumber ?? "").trim().toLowerCase();

        if (key) studentMap.set(key, s);

      });

    }



    const staffMap = new Map();

    if (Array.isArray(staffs)) {

      staffs.forEach(s => {

        const key = String(s.staffCode ?? s.staffId ?? s.id ?? "").trim().toLowerCase();

        if (key) staffMap.set(key, s);

      });

    }



    const bookMap = new Map();

    if (Array.isArray(books)) {

      books.forEach(b => {

        const keys = [String(b.bookId ?? "").trim(), String(b.id ?? "").trim()].filter(Boolean);

        keys.forEach(key => bookMap.set(key, b));

      });

    }



    const rows = returnedIssues.map(issue => {

      const issueId = Number(issue.issueId ?? issue.id);

      const memberId = String(issue.memberId ?? issue.studrollNumber ?? issue.rollNumber ?? "").trim();

      const issueBookId = String(issue.bookId ?? issue.id ?? "").trim();

      const student = memberId ? studentMap.get(memberId.toLowerCase()) : null;

      const staff = memberId ? staffMap.get(memberId.toLowerCase()) : null;

      const issueMemberTypeRaw = String(issue.memberType ?? "").trim().toUpperCase();

      const rowMemberType = issueMemberTypeRaw === "EMPLOYEE" || issueMemberTypeRaw === "EMPLOYEES"

        ? MEMBER_TYPES.EMPLOYEES

        : (staff ? MEMBER_TYPES.EMPLOYEES : MEMBER_TYPES.STUDENTS);

      if (memberTypeFilter !== rowMemberType) return null;

      const member = rowMemberType === MEMBER_TYPES.EMPLOYEES ? staff : (student || {});

      const book = bookMap.get(issueBookId) || {};



      const dueDateObj = parseDateOnly(issue.dueDate);

      const returnDateObj = parseDateOnly(issue.returnDate);

      const lateDays = dueDateObj && returnDateObj ? Math.max(0, dayDiff(dueDateObj, returnDateObj)) : 0;

      const fineAmount = Number.isFinite(Number(issue.fineAmount)) ? Number(issue.fineAmount) : lateDays * RETURN_FINE_PER_DAY;

      const paymentStatus = getIssuePaymentStatus(issue, issueId, fineAmount);

      if (paymentStatus === "LATE_UNPAID") return null;



      return {

        memberName: member.name || issue.studentName || "-",

        rollNo: memberId || "-",

        year: rowMemberType === MEMBER_TYPES.EMPLOYEES

          ? (member.staffType || member.role || "-")

          : (member.year || member.Year || member.studentYear || issue.year || issue.studentYear || "-"),

        dept: member.department || member.dept || member.branch || issue.department || issue.dept || "-",

        bookName: book.title || issue.bookName || "-",

        author: book.author || issue.author || "-",

        category: book.category || issue.category || "-",

        issueDate: issue.issueDate || "-",

        returnDate: issue.returnDate || "-",

        lateDays,

        status: paymentStatus,

        memberType: rowMemberType,

        paymentMethod: paymentStatus === "ON_TIME" ? "-" : getIssuePaymentMethod(issue, issueId)

      };

    }).filter(Boolean);



    if (!rows.length) {

      tbody.innerHTML = makeEmptyStateRow(

        12,

        `No returned books for ${memberTypeFilter === MEMBER_TYPES.EMPLOYEES ? "employees" : "students"}.`,

        "Late returns appear only after fine payment.",

        "Receipt",

        "Pending Fines",

        "pendingFines"

      );

      return;

    }



    tbody.innerHTML = rows.map(row => `

      <tr class="member-row" data-status="${row.status === "ON_TIME" ? "ON_TIME" : "FINE_PAID"}" data-member-type="${row.memberType}">

        <td>${escapeHtml(row.memberName)}</td>

        <td>${escapeHtml(row.rollNo)}</td>

        <td class="member-year-col">${escapeHtml(row.year)}</td>

        <td class="member-dept-col">${escapeHtml(row.dept)}</td>

        <td>${escapeHtml(row.bookName)}</td>

        <td>${escapeHtml(row.author)}</td>

        <td>${escapeHtml(row.category)}</td>

        <td>${escapeHtml(row.issueDate)}</td>

        <td>${escapeHtml(row.returnDate)}</td>

        <td>${escapeHtml(row.lateDays)}</td>

        <td>${row.status === "ON_TIME" ? makeStatusBadge("returned", "RETURNED") : makeStatusBadge("paid", "RETURNED (FINE PAID)")}</td>

        <td>${escapeHtml(row.paymentMethod)}</td>

      </tr>

    `).join("");

    filterReturnedBooksTable();

  } catch (err) {

    console.error(err);

    setTableLoadError(tbody, 12, "Failed to load returned books", "Please retry after checking server status.");
    addNotification("Returned books load failed. Please check backend availability.", "error");

  }

}

function setPendingFinesStatus(message, isError = true) {

  const el = document.getElementById("pendingFinesStatus");

  if (!el) return;

  el.style.color = isError ? "#b91c1c" : "#166534";

  el.textContent = message || "";

}



function openFineDetail(issueId) {

  const row = pendingFineRows.find(r => Number(r.issueId) === Number(issueId));

  if (!row) {
    // If the selected fine is no longer pending (e.g., paid), clear detail panel.
    closeFineDetail();
    setPendingFinesStatus("Fine record not found. It may have been paid already.");
    selectedFineIssueId = null;
    return;
  }

  selectedFineIssueId = Number(issueId);



  const card = document.getElementById("fineDetailCard");

  if (!card) return;



  const methodEl = document.getElementById("detailFineMethod");

  const payBtn = document.getElementById("detailPayFineBtn");

  const statusText = row.paymentStatus === "PAID" || row.paymentStatus === "FINE_PAID" ? "PAID" : "PENDING";

  const method = row.paymentMethod && row.paymentMethod !== "-" ? row.paymentMethod : "CASH";



  document.getElementById("fineDetailMember").textContent = `Member ID: ${row.rollNo}${row.memberType ? ` (${row.memberType})` : ""}`;

  document.getElementById("fineDetailName").textContent = `Name: ${row.studentName}`;

  document.getElementById("fineDetailBook").textContent = `Book Title: ${row.bookTitle}`;

  document.getElementById("fineDetailDays").textContent = `Overdue Days: ${row.overdueDays}`;

  document.getElementById("fineDetailAmount").textContent = `Fine Amount: ${row.fineAmount}`;

  document.getElementById("fineDetailStatus").textContent = `Payment Status: ${statusText}`;



  if (methodEl) {

    methodEl.value = method === "GPAY" ? "GPAY" : "CASH";

    methodEl.disabled = row.paymentStatus === "PAID" || row.paymentStatus === "FINE_PAID";

  }



  if (payBtn) {

    payBtn.disabled = row.paymentStatus === "PAID" || row.paymentStatus === "FINE_PAID";

    payBtn.textContent = row.paymentStatus === "PAID" || row.paymentStatus === "FINE_PAID" ? "Paid" : "Pay Fine";

  }



  card.classList.remove("hidden");

}



function closeFineDetail() {

  selectedFineIssueId = null;

  document.getElementById("fineDetailCard")?.classList.add("hidden");

}



function payFineFromDetail() {

  if (!Number.isInteger(selectedFineIssueId)) {

    setPendingFinesStatus("Select a student fine record first.");

    return;

  }

  const method = String(document.getElementById("detailFineMethod")?.value || "").trim().toUpperCase();

  payFine(selectedFineIssueId, method);

}



async function loadPendingFines() {

  const tbody = document.getElementById("pendingFinesTableBody");

  if (!tbody) return;



  tbody.innerHTML = `<tr><td colspan="7"><span class="loader-spinner" style="display:inline-block;vertical-align:middle;margin-right:8px;"></span>Loading fine records...</td></tr>`;

  setPendingFinesStatus("");

  pendingFineRows = [];



  try {

    const res = await fetch(`${BASE_URL}/api/issue/pending-fines`);

    if (!res.ok) {

      throw new Error(await res.text());

    }

    const pendingRows = await res.json();



    pendingFineRows = Array.isArray(pendingRows) ? pendingRows.map(row => ({

      issueId: Number(row.issueId ?? row.id),

      rollNo: String(row.memberId ?? row.rollNo ?? row.studrollNumber ?? row.rollNumber ?? row.member_id ?? "-"),

      memberType: String(row.memberType ?? "").trim().toUpperCase() || "-",

      studentName: String(row.memberName ?? row.studentName ?? "-"),

      bookTitle: String(row.bookTitle ?? "-"),

      overdueDays: Number(row.overdueDays ?? row.overdue ?? 0),

      fineAmount: Number(row.fineAmount ?? 0),

      paymentStatus: String(row.paymentStatus ?? "-").trim().toUpperCase(),

      paymentMethod: String(row.paymentMethod ?? "-").trim().toUpperCase()

    })) : [];



    if (!pendingFineRows.length) {

      tbody.innerHTML = makeEmptyStateRow(7, "No fine records found", "No overdue fines are pending right now.", "💳");

      closeFineDetail();

      return;

    }



    tbody.innerHTML = pendingFineRows.map(row => `

      <tr data-status="${row.paymentStatus === "PAID" || row.paymentStatus === "FINE_PAID" ? "PAID" : "PENDING"}">

        <td>${escapeHtml(row.rollNo)}</td>

        <td>${escapeHtml(row.studentName)}</td>

        <td>${escapeHtml(row.bookTitle)}</td>

        <td>${escapeHtml(row.overdueDays)}</td>

        <td>${escapeHtml(row.fineAmount)}</td>

        <td>${row.paymentStatus === "PAID" || row.paymentStatus === "FINE_PAID" ? makeStatusBadge("paid", "PAID") : makeStatusBadge("pending", "PENDING")}</td>

        <td><button type="button" onclick="openFineDetail(${row.issueId})">View</button></td>

      </tr>

    `).join("");

    filterPendingFinesTable();



    if (Number.isInteger(selectedFineIssueId)) {

      const found = pendingFineRows.some(r => Number(r.issueId) === Number(selectedFineIssueId));
      if (found) {
        openFineDetail(selectedFineIssueId);
      } else {
        closeFineDetail();
        setPendingFinesStatus("Previously selected fine is no longer pending.");
      }

    }

  } catch (err) {

    console.error(err);

    setTableLoadError(tbody, 7, "Failed to load pending fines", "Verify backend issue/fine API.");
    setPendingFinesStatus("Unable to load pending fines at this time. Check server connection.");
    addNotification("Pending fines load failed. Check backend API and retry.", "error");

  }

}



async function payFine(issueId, selectedMethod) {

  const method = String(selectedMethod || "").trim().toUpperCase();

  if (method !== "CASH" && method !== "GPAY") {

    setPendingFinesStatus("Choose payment method (Cash or GPay).");

    return;

  }



  const ok = await confirmAction("Pay Fine", `Confirm payment via ${method}?`, "Pay");

  if (!ok) return;



  setGlobalLoading(true, "Updating payment...");

  try {

    const params = new URLSearchParams({

      issueId: String(issueId),

      paymentMethod: method

    });

    const res = await fetch(`${BASE_URL}/api/issue/pay-fine?${params.toString()}`, { method: "POST" });

    if (!res.ok) {

      const errText = await res.text();

      setPendingFinesStatus(`Payment failed. ${errText || "Try again."}`);

      return;

    }



    setPendingFinesStatus(`Fine paid successfully for issue ${issueId} via ${method}.`, false);

    addNotification(`Fine paid for issue ${issueId} via ${method}.`);

    await loadPendingFines();

    await loadReturnedBooks();

    openFineDetail(issueId);

  } finally {

    setGlobalLoading(false);

  }

}



function filterRowsBySearchAndStatus(tbodyId, searchId, statusId) {

  const tbody = document.getElementById(tbodyId);

  if (!tbody) return;



  const searchValue = String(document.getElementById(searchId)?.value || "").trim().toLowerCase();

  const statusValue = String(document.getElementById(statusId)?.value || "ALL").trim().toUpperCase();



  Array.from(tbody.querySelectorAll("tr")).forEach(row => {

    const text = row.innerText.toLowerCase();

    const rowStatus = String(row.getAttribute("data-status") || "").toUpperCase();

    const searchOk = !searchValue || text.includes(searchValue);

    const statusOk = statusValue === "ALL" || rowStatus === statusValue;

    row.style.display = searchOk && statusOk ? "" : "none";

  });

}



function filterIssuedBooksTable() {

  filterRowsBySearchAndStatus("issuedBooksTableBody", "issuedSearch", "issuedFilter");

}



function filterReturnedBooksTable() {

  filterRowsBySearchAndStatus("returnedBooksTableBody", "returnedSearch", "returnedFilter");

}



function filterPendingFinesTable() {

  filterRowsBySearchAndStatus("pendingFinesTableBody", "pendingFineSearch", "pendingFineFilter");

}













function showSection(id, el) {

  // hide all sections

  document.querySelectorAll('.section').forEach(s =>

    s.classList.remove('active')

  );



  // show selected section

  document.getElementById(id).classList.add('active');



  // menu active state

  document.querySelectorAll('.menu li').forEach(m =>

    m.classList.remove('active')

  );

  if (el) el.classList.add('active');

  hideNotifications();

  closeSidebarOnCompact();



  // 🔥 LOAD DATA BASED ON SECTION

  if (id === 'users') {

    setUserView(activeUserView);

  }



  if (id === 'books') {

    //loadBooks();    // later

  }



  if (id === 'allBooks') {

    openBooks();

  }



  if (id === 'issueBook') {

    setDefaultIssueDate();

    setMemberInputPlaceholders();

    setIssueStatus("");

    hideIssueToast();

  }



  if (id === 'returnBook') {

    setMemberInputPlaceholders();

    resetReturnSectionState();

    setDefaultReturnDate();

    setReturnStatus("");

  }



  if (id === 'pendingBooks') {

    document.getElementById("issuedBooksTable")?.classList.toggle("hide-year", (sectionMemberTabs.pendingBooks || MEMBER_TYPES.STUDENTS) === MEMBER_TYPES.EMPLOYEES);

    loadIssuedBooks();

  }



  if (id === 'returnedBooks') {

    document.getElementById("returnedBooksTable")?.classList.toggle("hide-year", (sectionMemberTabs.returnedBooks || MEMBER_TYPES.STUDENTS) === MEMBER_TYPES.EMPLOYEES);

    loadReturnedBooks();

  }



  if (id === 'pendingFines') {

    loadPendingFines();

  }



  if (id === 'dashboard') {

    loadDashboardOverview();

  }

}





function filterTable(input,tableId){

  const value=input.value.toLowerCase();

  document.querySelectorAll(`#${tableId} tr`).forEach((row,i)=>{

    if(i>0){

      row.style.display=row.innerText.toLowerCase().includes(value) ? "" : "none";

    }

  });

}



setUserView(activeUserView);



async function logout() {

  let canLogout = true;

  if (window.Swal) {

    const result = await Swal.fire({

      icon: "warning",

      title: "Logout Now?",

      text: "You can login again anytime.",

      showCancelButton: true,

      confirmButtonText: "Logout",

      cancelButtonText: "Stay Here",

      confirmButtonColor: "#dc2626"

    });

    canLogout = result.isConfirmed;

  } else {

    canLogout = window.confirm("Logout now?");

  }



  if (!canLogout) return;



  if (window.Swal) {

    await Swal.fire({

      icon: "success",

      title: "See you soon",

      text: "Logging out securely...",

      timer: 900,

      showConfirmButton: false

    });

  }

  window.location.href = "index.html";

}



window.addEventListener("DOMContentLoaded", () => {  renderNotifications();

  setMemberInputPlaceholders();

  loadDashboardOverview();

  setAddUserTab(MEMBER_TYPES.STUDENTS);

  setInterval(() => {

    const dashboardActive = document.getElementById("dashboard")?.classList.contains("active");

    if (dashboardActive) {

      loadDashboardOverview();

    }

  }, 30000);

});



window.addEventListener("resize", () => {

  if (!isCompactLayout()) {

    document.body.classList.remove("sidebar-open");

  }

});



document.addEventListener("keydown", (event) => {

  if (event.key === "Escape") {

    document.body.classList.remove("sidebar-open");

  }

});



document.addEventListener("click", (event) => {

  const panel = document.getElementById("notificationsPanel");

  const bellWrap = document.querySelector(".bell-wrap");

  if (!panel || !bellWrap) return;

  if (panel.contains(event.target) || bellWrap.contains(event.target)) return;

  panel.classList.remove("show");

});





