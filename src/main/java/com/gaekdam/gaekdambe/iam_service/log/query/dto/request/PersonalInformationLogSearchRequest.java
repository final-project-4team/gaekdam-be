package com.gaekdam.gaekdambe.iam_service.log.query.dto.request;

import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import java.time.LocalDateTime;

public record PersonalInformationLogSearchRequest(
        Long hotelGroupCode,
        String accessorLoginId,
        PermissionTypeKey permissionTypeKey,
        String purpose,
        LocalDateTime fromDate,
        LocalDateTime toDate) {
}
