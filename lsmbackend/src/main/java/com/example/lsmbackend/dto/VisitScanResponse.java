package com.example.lsmbackend.dto;

public record VisitScanResponse(
        String action,
        String message,
        VisitRecordDto record
) {
}
