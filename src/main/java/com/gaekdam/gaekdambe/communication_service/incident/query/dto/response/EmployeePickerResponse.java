package com.gaekdam.gaekdambe.communication_service.incident.query.dto.response;

public record EmployeePickerResponse(
        Long employeeCode,
        String employeeName,
        String loginId
) {}