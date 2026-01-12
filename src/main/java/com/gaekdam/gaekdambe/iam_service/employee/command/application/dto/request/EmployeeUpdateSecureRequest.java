package com.gaekdam.gaekdambe.iam_service.employee.command.application.dto.request;

import com.gaekdam.gaekdambe.iam_service.employee.command.domain.EmployeeStatus;

public record EmployeeUpdateSecureRequest(
        String name,
        String phoneNumber,
        String email,
        Long departmentCode,
        Long positionCode,
        Long propertyCode,
        Long hotelGroupCode,
        Long permissionCode,
        EmployeeStatus employeeStatus) {
}
