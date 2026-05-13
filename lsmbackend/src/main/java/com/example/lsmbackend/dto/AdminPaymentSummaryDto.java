package com.example.lsmbackend.dto;

import java.util.List;

public record AdminPaymentSummaryDto(
        double totalPaidByStudents,
        double totalPaidByGpay,
        double totalPaidByCash,
        int studentsWithPayments,
        List<StudentFinePaymentSummaryRowDto> studentPayments
) {
}
