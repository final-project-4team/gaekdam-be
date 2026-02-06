package com.gaekdam.gaekdambe.iam_service.log.query.dto.request;

import java.time.LocalDateTime;

public record PermissionChangedLogSearchRequest(
        Long hotelGroupCode,
        String accessorLoginId,
        String changedLoginId,
        String beforePermissionName,
        String afterPermissionName,
        LocalDateTime fromDate,
        LocalDateTime toDate) {
}
