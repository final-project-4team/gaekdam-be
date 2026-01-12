package com.gaekdam.gaekdambe.iam_service.employee.query.dto.response;

public record EmployeeListEncResponse(
     Long employeeCode,
     Long employeeNumber,
     String loginId,
     byte[] employeeNameEnc,
     byte[] phoneNumberEnc,
     byte[] emailEnc,
     byte[] dekEnc
){
}
