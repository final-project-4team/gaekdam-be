package com.gaekdam.gaekdambe.iam_service.log.query.dto.response;

import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import java.time.LocalDateTime;

public record AuditLogQueryResponse(
                Long auditLogCode,
                PermissionTypeKey permissionTypeKey,
                Long employeeCode,
                String employeeLoginId,
                String employeeName,
                Long hotelGroupCode,
                String details,
                String previousValue,
                String newValue,
                LocalDateTime occurredAt) {
}
