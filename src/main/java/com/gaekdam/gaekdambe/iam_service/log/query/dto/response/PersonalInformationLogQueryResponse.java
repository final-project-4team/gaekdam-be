package com.gaekdam.gaekdambe.iam_service.log.query.dto.response;

import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import java.time.LocalDateTime;

public record PersonalInformationLogQueryResponse(
                Long personalInformationLogCode,
                LocalDateTime occurredAt,
                PermissionTypeKey permissionTypeKey,
                Long employeeAccessorCode,
                String employeeAccessorName,
                String employeeAccessorLoginId,
                String targetType,
                Long targetCode,
                String targetName,
                String purpose) {
}
