package com.gaekdam.gaekdambe.iam_service.employee.query.dto.response;


import com.gaekdam.gaekdambe.iam_service.employee.command.domain.EmployeeStatus;

public record EmployeeListResponse(
    Long code,
    String departmentName,
    String hotelPositionName,
    String name,
    String phone,
    String email,
    String loginId,
    EmployeeStatus employeeStatus
) {

}
