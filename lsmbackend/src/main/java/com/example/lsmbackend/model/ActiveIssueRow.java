package com.example.lsmbackend.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


public class ActiveIssueRow {
    private String studentName;
    private String rollNumber;
    private String year;
    private String department;

    private String bookName;
    private String author;
    private String category;

    private LocalDate issueDate;
    private LocalDate returnDate; // planned return date (dueDate)


    public ActiveIssueRow() {
    }

    public ActiveIssueRow(String studentName, String rollNumber, String year, String department, String bookName, String author, String category, LocalDate issueDate, LocalDate returnDate) {
        this.studentName = studentName;
        this.rollNumber = rollNumber;
        this.year = year;
        this.department = department;
        this.bookName = bookName;
        this.author = author;
        this.category = category;
        this.issueDate = issueDate;
        this.returnDate = returnDate;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }
}
