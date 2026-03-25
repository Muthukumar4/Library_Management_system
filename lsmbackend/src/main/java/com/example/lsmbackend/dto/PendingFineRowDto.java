package com.example.lsmbackend.dto;

public record PendingFineRowDto(
        Long issueId,
        String memberId,
        String memberName,
        String memberType,

        Long bookId,
        String bookTitle,
        long overdueDays,
        double fineAmount,
        String paymentStatus
) {
}
