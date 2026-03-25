package com.example.lsmbackend.controler;

import com.example.lsmbackend.dto.PendingFineRowDto;
import com.example.lsmbackend.dto.ReturnedBookRowDto;
import com.example.lsmbackend.model.Issuebook;
import com.example.lsmbackend.service.Issueservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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


    @PostMapping("/return")
    public Issuebook returnBook( @RequestParam Long issueId,
                                 @RequestParam Double fineAmount,
                                 @RequestParam(required = false) String paymentMethod
    ) {
        return issuesrv.returnBook(issueId,fineAmount,paymentMethod);
    }

    @GetMapping("/member/{memberId}")
    public List<Issuebook> getIssuesByStudent(@PathVariable String memberId) {
        return issuesrv.getIssuesByMember(memberId);
    }
    @GetMapping("/member/{memberType}/{memberId}")
    public List<Issuebook> getByMemberAndType(@PathVariable String memberType, @PathVariable String memberId) {
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



}
