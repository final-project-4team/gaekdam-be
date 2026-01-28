package com.gaekdam.gaekdambe.iam_service.log.query.dto.request;


import java.time.LocalDateTime;

public record AuditLogSearchRequest(
        Long hotelGroupCode,
        String employeeLoginId,
        String permissionTypeKey,
        String details,
        LocalDateTime fromDate,
        LocalDateTime toDate) {
}
