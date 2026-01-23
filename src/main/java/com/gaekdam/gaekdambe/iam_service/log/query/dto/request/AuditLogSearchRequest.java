package com.gaekdam.gaekdambe.iam_service.log.query.dto.request;

import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import java.time.LocalDateTime;

public record AuditLogSearchRequest(
        Long hotelGroupCode,
        String employeeLoginId,
        PermissionTypeKey permissionTypeKey,
        String details,
        LocalDateTime fromDate,
        LocalDateTime toDate) {
}
