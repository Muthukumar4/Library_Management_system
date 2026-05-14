package com.example.lsmbackend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record VisitRecordDto(
        Long id,
        String memberId,
        String name,
        String memberType,
        String department,
        LocalDateTime entryTime,
        LocalDateTime exitTime,
        String status,
        LocalDate date
) {
}
