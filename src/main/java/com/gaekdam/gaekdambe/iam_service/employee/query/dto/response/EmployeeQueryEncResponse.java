package com.gaekdam.gaekdambe.iam_service.employee.query.dto.response;

import java.time.LocalDateTime;

public record EmployeeQueryEncResponse(
        Long employeeCode,
        Long employeeNumber,
        String loginId,
        byte[] employeeNameEnc,
        byte[] phoneNumberEnc,
        byte[] emailEnc,
        byte[] dekEnc,

        // JOIN 필드
        String departmentName,
        String hotelPositionName,
        String propertyName,
        String hotelGroupName,
        String permissionName,

        // 기타 엔티티 필드
        LocalDateTime hiredAt,
        String employeeStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long departmentCode,
        Long hotelPositionCode,
        Long propertyCode,
        Long hotelGroupCode,
        Long permissionCode,
        int failedLoginCount,
        LocalDateTime lastLoginAt) {
}
