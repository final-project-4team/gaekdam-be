package com.gaekdam.gaekdambe.iam_service.employee.query.dto.response;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmployeeDetailResponse {
  private Long employeeCode;
  private Long employeeNumber;
  private String loginId;
  private byte[] employeeNameEnc;
  private byte[] phoneNumberEnc;
  private byte[] emailEnc;
  private byte[] dekEnc;
}
