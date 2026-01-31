package com.gaekdam.gaekdambe.iam_service.employee.query.dto.response;

import java.time.LocalDateTime;

public record EmployeeDetailResponse(
        Long employeeCode,
        Long employeeNumber,
        String loginId,
        String employeeName,
        String phoneNumber,
        String email,

        // JOIN 필드 추가
        String departmentName,
        String hotelPositionName,
        String propertyName,
        String hotelGroupName,
        String permissionName,

        Long departmentCode,
        Long hotelPositionCode,
        Long propertyCode,
        Long hotelGroupCode,
        Long permissionCode,

        // 기타 필드
        LocalDateTime hiredAt,
        String employeeStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        int failedLoginCount,
        LocalDateTime lastLoginAt) {
}
