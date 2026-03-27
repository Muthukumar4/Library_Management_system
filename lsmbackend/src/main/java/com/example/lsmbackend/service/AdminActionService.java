package com.example.lsmbackend.service;

import com.example.lsmbackend.dto.AdminBookUpdateRequest;
import com.example.lsmbackend.dto.AdminDeleteRequest;
import com.example.lsmbackend.dto.AdminStaffUpdateRequest;
import com.example.lsmbackend.dto.AdminStudentUpdateRequest;
import com.example.lsmbackend.model.AdminAuditLog;
import com.example.lsmbackend.model.Book;
import com.example.lsmbackend.model.Staff;
import com.example.lsmbackend.model.StaffType;
import com.example.lsmbackend.model.Student;
import com.example.lsmbackend.repository.AdminAuditLogRepository;
import com.example.lsmbackend.repository.Bookrepo;
import com.example.lsmbackend.repository.Staffrepo;
import com.example.lsmbackend.repository.Studerepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AdminActionService {

    @Autowired
    private Studerepo studerepo;

    @Autowired
    private Staffrepo staffrepo;

    @Autowired
    private Bookrepo bookrepo;

    @Autowired
    private AdminAuditLogRepository auditLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    public void validateAdmin(String username, String password, String reason) {
        if (username == null || username.isBlank()) {
            throw new RuntimeException("Admin username is required");
        }
        if (password == null || password.isBlank()) {
            throw new RuntimeException("Admin password is required");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("Reason is required");
        }
        if (!adminUsername.equals(username.trim()) || !adminPassword.equals(password)) {
            throw new RuntimeException("Invalid admin credentials");
        }
    }

    public boolean loginAdmin(String username, String password) {
        return adminUsername.equals(username == null ? "" : username.trim())
                && adminPassword.equals(password == null ? "" : password);
    }

    public Student updateStudent(String rollNumber, AdminStudentUpdateRequest request) {
        validateAdmin(request.getAdminUsername(), request.getAdminPassword(), request.getReason());
        Student student = studerepo.findByRollNumber(rollNumber)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        String before = toJson(student);

        student.setName(request.getName());
        student.setDepartment(request.getDepartment());
        student.setYear(request.getYear());
        student.setEmail(request.getEmail());
        student.setPhone(request.getPhone());
        student.setActive(request.isActive());

        Student saved = studerepo.save(student);
        saveAudit("UPDATE", "STUDENT", saved.getRollNumber(), request.getAdminUsername(), request.getReason(), before, toJson(saved));
        return saved;
    }

    public void deleteStudent(String rollNumber, AdminDeleteRequest request) {
        validateAdmin(request.getAdminUsername(), request.getAdminPassword(), request.getReason());
        Student student = studerepo.findByRollNumber(rollNumber)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        saveAudit("DELETE", "STUDENT", student.getRollNumber(), request.getAdminUsername(), request.getReason(), toJson(student), null);
        studerepo.delete(student);
    }

    public Staff updateStaff(String staffCode, AdminStaffUpdateRequest request) {
        validateAdmin(request.getAdminUsername(), request.getAdminPassword(), request.getReason());
        Staff staff = staffrepo.findByStaffCode(staffCode)
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        String before = toJson(staff);

        staff.setName(request.getName());
        staff.setDepartment(request.getDepartment());
        staff.setEmail(request.getEmail());
        staff.setPhone(request.getPhone());
        staff.setActive(request.isActive());
        if (request.getStaffType() != null && !request.getStaffType().isBlank()) {
            staff.setStaffType(StaffType.valueOf(request.getStaffType().trim()));
        }

        Staff saved = staffrepo.save(staff);
        saveAudit("UPDATE", "STAFF", saved.getStaffCode(), request.getAdminUsername(), request.getReason(), before, toJson(saved));
        return saved;
    }

    public void deleteStaff(String staffCode, AdminDeleteRequest request) {
        validateAdmin(request.getAdminUsername(), request.getAdminPassword(), request.getReason());
        Staff staff = staffrepo.findByStaffCode(staffCode)
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        saveAudit("DELETE", "STAFF", staff.getStaffCode(), request.getAdminUsername(), request.getReason(), toJson(staff), null);
        staffrepo.delete(staff);
    }

    public Book updateBook(Long bookId, AdminBookUpdateRequest request) {
        validateAdmin(request.getAdminUsername(), request.getAdminPassword(), request.getReason());
        Book book = bookrepo.findByBookId(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        String before = toJson(book);

        Integer totalCopies = request.getTotalCopies();
        Integer availableCopies = request.getAvailableCopies();
        if (totalCopies == null || totalCopies < 0) {
            throw new RuntimeException("Total copies must be 0 or more");
        }
        if (availableCopies == null || availableCopies < 0) {
            throw new RuntimeException("Available copies must be 0 or more");
        }
        if (availableCopies > totalCopies) {
            throw new RuntimeException("Available copies cannot exceed total copies");
        }

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setPublisher(request.getPublisher());
        book.setCategory(request.getCategory());
        book.setRackNumber(request.getRackNumber());
        book.setShelfNumber(request.getShelfNumber());
        book.setTotalcopies(totalCopies);
        book.setAvailablecopies(availableCopies);
        book.setAvailable(request.isAvailable());

        Book saved = bookrepo.save(book);
        saveAudit("UPDATE", "BOOK", String.valueOf(saved.getBookId()), request.getAdminUsername(), request.getReason(), before, toJson(saved));
        return saved;
    }

    public void deleteBook(Long bookId, AdminDeleteRequest request) {
        validateAdmin(request.getAdminUsername(), request.getAdminPassword(), request.getReason());
        Book book = bookrepo.findByBookId(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        saveAudit("DELETE", "BOOK", String.valueOf(book.getBookId()), request.getAdminUsername(), request.getReason(), toJson(book), null);
        bookrepo.delete(book);
    }

    private void saveAudit(String actionType, String targetType, String targetKey, String username, String reason, String beforeData, String afterData) {
        AdminAuditLog log = new AdminAuditLog();
        log.setActionType(actionType);
        log.setTargetType(targetType);
        log.setTargetKey(targetKey);
        log.setAdminUsername(username);
        log.setReason(reason.trim());
        log.setBeforeData(beforeData);
        log.setAfterData(afterData);
        auditLogRepository.save(log);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize audit data", e);
        }
    }
}
