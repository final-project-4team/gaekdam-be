package com.gaekdam.gaekdambe.iam_service.log.query.dto.response;

import java.time.LocalDateTime;

public record PermissionChangedLogQueryResponse(
        Long permissionChangedLogCode,
        LocalDateTime changedAt,
        Long employeeAccessorCode,
        String employeeAccessorName,
        String employeeAccessorLoginId,
        Long employeeChangedCode,
        String employeeChangedName,
        String employeeChangedLoginId,
        Long hotelGroupCode,
        Long beforePermissionCode,
        String beforePermissionName,
        Long afterPermissionCode,
        String afterPermissionName) {
}
