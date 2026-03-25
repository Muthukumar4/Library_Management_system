package com.example.lsmbackend.controler;


public class IssueRequest {
    private Long studrollNumber;
    private Long bookId;

    public Long getStudrollNumber() {
        return studrollNumber;
    }

    public void setStudrollNumber(Long studrollNumber) {
        this.studrollNumber = studrollNumber;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
}
