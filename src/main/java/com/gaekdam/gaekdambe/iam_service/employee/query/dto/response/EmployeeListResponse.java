package com.gaekdam.gaekdambe.iam_service.employee.query.dto.response;

import com.gaekdam.gaekdambe.iam_service.employee.command.domain.EmployeeStatus;

public record EmployeeListResponse(
        Long employeeCode,
        Long employeeNumber,

        String permissionName,
        String employeeName,
        String phoneNumber,
        String email,
        String loginId,
        EmployeeStatus employeeStatus) {

}
