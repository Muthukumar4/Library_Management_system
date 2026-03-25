package com.example.lsmbackend.service;

import com.example.lsmbackend.dto.PendingFineRowDto;
import com.example.lsmbackend.dto.ReturnedBookRowDto;
import com.example.lsmbackend.model.ActiveIssueRow;
import com.example.lsmbackend.model.Book;
import com.example.lsmbackend.model.Issuebook;
import com.example.lsmbackend.repository.Bookrepo;
import com.example.lsmbackend.repository.Issuerepo;
import com.example.lsmbackend.repository.Staffrepo;
import com.example.lsmbackend.repository.Studerepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class Issueservice {

    @Autowired
    private Issuerepo issuerepo;

    @Autowired
    private Bookrepo bookrepo;

    @Autowired
    private Studerepo studentrepo;

    @Autowired
    private Staffrepo staffrepo;

    public Issuebook issueBook(String memberId, String memberType, Long bookId, LocalDate issueDate, LocalDate dueDate) {
        Book book = bookrepo.findByBookId(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (book.getAvailablecopies() <= 0) {
            throw new RuntimeException("Book not available");
        }

        String type = normalizeMemberType(memberType);
        if (memberId == null || memberId.trim().isEmpty()) {
            throw new RuntimeException("Member ID is required");
        }

        // Optional validation: ensure member exists
        validateMemberExists(memberId.trim(), type);

        book.setAvailablecopies(book.getAvailablecopies() - 1);
        bookrepo.save(book);

        Issuebook record = new Issuebook();
        record.setMemberId(memberId.trim());
        record.setMemberType(type); // STUDENT / EMPLOYEE
        record.setBookId(bookId);
        record.setIssueDate(issueDate != null ? issueDate : LocalDate.now());
        record.setDueDate(dueDate != null ? dueDate : LocalDate.now().plusDays(14));
        record.setStatus("ISSUED");
        record.setFineAmount(0.0);
        record.setPaymentStatus("PENDING");
        record.setPaymentMethod(null);

        return issuerepo.save(record);
    }

    public Issuebook returnBook(Long issueId, Double fineAmount, String paymentMethod) {
        Issuebook record = issuerepo.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue record not found"));

        if ("RETURNED".equalsIgnoreCase(record.getStatus())) {
            throw new RuntimeException("Book already returned");
        }

        Book book = bookrepo.findByBookId(record.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found"));

        book.setAvailablecopies(book.getAvailablecopies() + 1);
        bookrepo.save(book);

        record.setReturnDate(LocalDate.now());
        record.setStatus("RETURNED");

        double fine = fineAmount == null ? 0.0 : fineAmount;
        record.setFineAmount(fine);

        if (fine > 0) {
            record.setPaymentStatus("PENDING");
            record.setPaymentMethod(null);
        } else {
            record.setPaymentStatus("ON_TIME");
            record.setPaymentMethod("-");
        }

        return issuerepo.save(record);
    }

    public Issuebook payFine(Long issueId, String paymentMethod) {
        Issuebook record = issuerepo.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue record not found"));

        if (!"RETURNED".equalsIgnoreCase(record.getStatus())) {
            throw new RuntimeException("Book not returned yet");
        }
        if (record.getFineAmount() == null || record.getFineAmount() <= 0) {
            throw new RuntimeException("No fine to pay");
        }

        String method = paymentMethod == null ? "" : paymentMethod.trim().toUpperCase(Locale.ROOT);
        if (!method.equals("CASH") && !method.equals("GPAY")) {
            throw new RuntimeException("Invalid payment method");
        }

        record.setPaymentStatus("PAID");
        record.setPaymentMethod(method);
        return issuerepo.save(record);
    }

    public List<Issuebook> getIssuesByMember(String memberId) {
        return issuerepo.findByMemberId(memberId);
    }

    public List<Issuebook> getIssuesByMemberAndType(String memberId, String memberType) {
        return issuerepo.findByMemberIdAndMemberType(memberId, normalizeMemberType(memberType));
    }

    public List<Issuebook> getAllIssueBook() {
        return issuerepo.findAll();
    }

    public List<ActiveIssueRow> getActiveIssuedBooks() {
        return issuerepo.findAll().stream()
                .filter(i -> {
                    String status = i.getStatus() == null ? "" : i.getStatus().trim().toUpperCase(Locale.ROOT);
                    return !"RETURNED".equals(status) && i.getReturnDate() == null;
                })
                .map(i -> {
                    MemberView member = resolveMember(i.getMemberId(), i.getMemberType());
                    Book book = bookrepo.findByBookId(i.getBookId()).orElse(null);

                    return new ActiveIssueRow(
                            member.name,
                            i.getMemberId(),
                            member.yearOrType,
                            member.department,
                            book != null ? book.getTitle() : "-",
                            book != null ? book.getAuthor() : "-",
                            book != null ? book.getCategory() : "-",
                            i.getIssueDate(),
                            i.getDueDate()
                    );
                })
                .collect(Collectors.toList());
    }

    public List<PendingFineRowDto> getPendingFines() {
        return issuerepo.findByStatus("RETURNED").stream()
                .filter(i -> i.getFineAmount() != null && i.getFineAmount() > 0)
                .filter(i -> !"PAID".equalsIgnoreCase(i.getPaymentStatus()))
                .map(i -> {
                    MemberView member = resolveMember(i.getMemberId(), i.getMemberType());
                    Book b = bookrepo.findByBookId(i.getBookId()).orElse(null);

                    long overdue = 0;
                    if (i.getDueDate() != null && i.getReturnDate() != null) {
                        overdue = ChronoUnit.DAYS.between(i.getDueDate(), i.getReturnDate());
                        if (overdue < 0) overdue = 0;
                    }

                    return new PendingFineRowDto(
                            i.getIssueId(),
                            i.getMemberId(),
                            member.name,
                            member.yearOrType,
                            i.getBookId(),
                            b != null ? b.getTitle() : "-",
                            overdue,
                            i.getFineAmount(),
                            i.getPaymentStatus()
                    );
                })
                .toList();
    }

    public List<ReturnedBookRowDto> getReturnedBooks() {
        return issuerepo.findByStatus("RETURNED").stream()
                .filter(i -> {
                    boolean onTime = i.getFineAmount() == null || i.getFineAmount() <= 0;
                    boolean paidLate = "PAID".equalsIgnoreCase(i.getPaymentStatus());
                    return onTime || paidLate;
                })
                .map(i -> {
                    MemberView member = resolveMember(i.getMemberId(), i.getMemberType());
                    Book b = bookrepo.findByBookId(i.getBookId()).orElse(null);

                    long late = 0;
                    if (i.getDueDate() != null && i.getReturnDate() != null) {
                        late = ChronoUnit.DAYS.between(i.getDueDate(), i.getReturnDate());
                        if (late < 0) late = 0;
                    }

                    return new ReturnedBookRowDto(
                            i.getIssueId(),
                            i.getMemberId(),
                            member.name,
                            member.yearOrType,
                            member.department,
                            i.getMemberType(),
                            i.getBookId(),
                            b != null ? b.getTitle() : "-",
                            b != null ? b.getAuthor() : "-",
                            b != null ? b.getCategory() : "-",
                            i.getIssueDate(),
                            i.getReturnDate(),
                            late,
                            (i.getFineAmount() == null || i.getFineAmount() <= 0) ? "ON_TIME" : "FINE_PAID",
                            i.getPaymentMethod() == null ? "-" : i.getPaymentMethod()
                    );
                })
                .toList();
    }

    private void validateMemberExists(String memberId, String memberType) {
        if ("EMPLOYEE".equals(memberType)) {
            boolean ok = staffrepo.findByStaffCode(memberId).isPresent();
            if (!ok) throw new RuntimeException("Employee not found");
        } else {
            boolean ok = studentrepo.findByRollNumber(memberId).isPresent();
            if (!ok) throw new RuntimeException("Student not found");
        }
    }

    private MemberView resolveMember(String memberId, String memberType) {
        String type = normalizeMemberType(memberType);

        if ("EMPLOYEE".equals(type)) {
            var st = staffrepo.findByStaffCode(memberId).orElse(null);
            return new MemberView(
                    st != null ? st.getName() : "-",
                    st != null ? (st.getStaffType() != null ? String.valueOf(st.getStaffType()) : "-") : "-",
                    st != null ? (st.getDepartment() != null ? st.getDepartment() : "-") : "-"
            );
        }

        var s = studentrepo.findByRollNumber(memberId).orElse(null);
        return new MemberView(
                s != null ? s.getName() : "-",
                s != null ? (s.getYear() != null ? s.getYear() : "-") : "-",
                s != null ? (s.getDepartment() != null ? s.getDepartment() : "-") : "-"
        );
    }

    private String normalizeMemberType(String memberType) {
        String t = memberType == null ? "" : memberType.trim().toUpperCase(Locale.ROOT);
        if ("EMPLOYEE".equals(t) || "EMPLOYEES".equals(t) || "STAFF".equals(t)) return "EMPLOYEE";
        return "STUDENT";
    }

    private static class MemberView {
        String name;
        String yearOrType;
        String department;

        MemberView(String name, String yearOrType, String department) {
            this.name = name;
            this.yearOrType = yearOrType;
            this.department = department;
        }
    }
}
