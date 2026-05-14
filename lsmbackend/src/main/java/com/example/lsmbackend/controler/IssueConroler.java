package com.example.lsmbackend.controler;

import com.example.lsmbackend.dto.PendingFineRowDto;
import com.example.lsmbackend.dto.ReturnedBookRowDto;
import com.example.lsmbackend.model.Issuebook;
import com.example.lsmbackend.service.Issueservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/api/issue")
@RestController
@CrossOrigin("*")
public class IssueConroler {

    @Autowired
    private Issueservice issuesrv;

//    @PostMapping("/add")
//    public Issuebook issueBook(
//            @RequestParam Long memberId,
//            @RequestParam Long bookId) {
//
//        return issuesrv.issueBook(studrollNumber, bookId);
//    }

    @PostMapping("/add")
    public Issuebook issueBook(
            @RequestParam String memberId,
            @RequestParam String memberType,
            @RequestParam Long bookId,
            @RequestParam LocalDate issueDate,
            @RequestParam LocalDate dueDate
    ) {
        return issuesrv.issueBook(memberId, memberType,bookId, issueDate, dueDate);
    }

    @GetMapping("/all")
    public List<Issuebook> allIssuebook(){
        return issuesrv.getAllIssueBook();
    }

    @GetMapping("/staff-scope/{staffCode}")
    public List<Issuebook> getIssuesForStaffScope(@PathVariable String staffCode) {
        return issuesrv.getIssuesForStaffScope(staffCode);
    }


    @PostMapping("/return")
    public Issuebook returnBook( @RequestParam Long issueId,
                                 @RequestParam Double fineAmount,
                                 @RequestParam(required = false) String paymentMethod
    ) {
        return issuesrv.returnBook(issueId,fineAmount,paymentMethod);
    }

    @GetMapping("/member/{memberId}")
    public List<Issuebook> getIssuesByStudent(@PathVariable String memberId, Authentication authentication) {
        ensureMemberAccess(authentication, "STUDENT", memberId);
        return issuesrv.getIssuesByMember(memberId);
    }
    @GetMapping("/member/{memberType}/{memberId}")
    public List<Issuebook> getByMemberAndType(@PathVariable String memberType, @PathVariable String memberId, Authentication authentication) {
        ensureMemberAccess(authentication, memberType, memberId);
        return issuesrv.getIssuesByMemberAndType(memberId, memberType);
    }

    @GetMapping("/returned")
    public List<ReturnedBookRowDto> returnedBooks() {
        return issuesrv.getReturnedBooks();
    }

    @GetMapping("/pending-fines")
    public List<PendingFineRowDto> pendingFines() {
        return issuesrv.getPendingFines();
    }

    @PostMapping("/pay-fine")
    public Issuebook payFine(
            @RequestParam Long issueId,
            @RequestParam String paymentMethod
    ) {
        return issuesrv.payFine(issueId, paymentMethod);
    }



    private void ensureMemberAccess(Authentication authentication, String memberType, String memberId) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (isAdmin) {
            return;
        }

        boolean isStudent = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_STUDENT".equals(authority.getAuthority()));
        boolean isStaff = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_STAFF".equals(authority.getAuthority()));
        String normalizedType = memberType == null ? "" : memberType.trim().toUpperCase();
        String normalizedMemberId = memberId == null ? "" : memberId.trim();

        if (isStudent) {
            if (!normalizedType.isEmpty() && !"STUDENT".equals(normalizedType)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Students can only access their own issue records");
            }
            if (!authentication.getName().equalsIgnoreCase(normalizedMemberId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Students can only access their own issue records");
            }
            return;
        }

        if (isStaff) {
            if (!normalizedType.isEmpty() && !"STAFF".equals(normalizedType)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Staff can only access their own issue records from this endpoint");
            }
            if (!authentication.getName().equalsIgnoreCase(normalizedMemberId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Staff can only access their own issue records from this endpoint");
            }
        }
    }

}
