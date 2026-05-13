package com.example.lsmbackend.dto;

public record StudentFinePaymentSummaryRowDto(
        String rollNumber,
        String studentName,
        String department,
        String year,
        double totalPaid,
        double gpayPaid,
        double cashPaid
) {
}
