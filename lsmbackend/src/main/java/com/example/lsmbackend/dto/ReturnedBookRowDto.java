package com.example.lsmbackend.dto;

import java.time.LocalDate;

public record ReturnedBookRowDto(
        Long issueId,
        String memberId,
        String memberName,
        String year,
        String department,
        String memberType,
        Long bookId,

        String bookTitle,
        String author,
        String category,
        LocalDate issueDate,
        LocalDate returnDate,
        long lateDays,
        String paymentStatus,
        String paymentMethod
) {
}
