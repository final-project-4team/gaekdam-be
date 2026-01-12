package com.gaekdam.gaekdambe.iam_service.employee.query.dto.response;

import java.time.LocalDateTime;

public record EmployeeDetailResponse(
                Long employeeCode,
                Long employeeNumber,
                String loginId,
                String name,
                String phone,
                String email,

                // JOIN 필드 추가
                String departmentName,
                String hotelPositionName,
                String propertyName,
                String hotelGroupName,
                String permissionName,

                // 기타 필드
                LocalDateTime hiredAt,
                String employeeStatus,
                LocalDateTime createdAt,
                LocalDateTime updatedAt,
                int failedLoginCount,
                LocalDateTime lastLoginAt) {
}
