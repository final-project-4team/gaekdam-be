package com.gaekdam.gaekdambe.iam_service.employee.query.dto.response;

import com.gaekdam.gaekdambe.iam_service.employee.command.domain.EmployeeStatus;

public record EmployeeQueryListEncResponse(
          Long employeeCode,
          Long employeeNumber,

          byte[] employeeNameEnc,
          byte[] phoneNumberEnc,
          byte[] emailEnc,
          String loginId,
          EmployeeStatus employeeStatus,
          String permissionName,
          byte[] dekEnc) {
}
