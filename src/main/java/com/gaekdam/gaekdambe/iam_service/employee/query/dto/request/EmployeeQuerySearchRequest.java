package com.gaekdam.gaekdambe.iam_service.employee.query.dto.request;

import com.gaekdam.gaekdambe.iam_service.employee.command.domain.EmployeeStatus;


public record EmployeeQuerySearchRequest(
    String name,
    String phone,
    String email,
    Long employeeNumber,
    String departmentName,
    String hotelPositionName,
    EmployeeStatus employeeStatus,
    String permissionName
) {

}
