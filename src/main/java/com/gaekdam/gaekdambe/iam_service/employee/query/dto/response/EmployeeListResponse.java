package com.gaekdam.gaekdambe.iam_service.employee.query.dto.response;


public record EmployeeListResponse(
    Long code,
    Long employeeNumber,
    String loginId,
    String name,
    String phone,
    String email,
    String departmentName,
    String hotelPositionName,
    String employeeStatus
) {

}
