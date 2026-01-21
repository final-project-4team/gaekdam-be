package com.gaekdam.gaekdambe.iam_service.employee.command.application.dto.request;


public record EmployeeSecureRegistrationRequest (
  Long employeeNumber,
  String loginId,
  String password,
  String email,
  String phoneNumber,
  String name,
  Long departmentCode,
  Long positionCode,
  Long propertyCode,
  Long hotelGroupCode,
  Long permissionCode ){
}
