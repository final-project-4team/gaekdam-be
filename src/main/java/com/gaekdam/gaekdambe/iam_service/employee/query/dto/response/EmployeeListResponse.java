package com.gaekdam.gaekdambe.iam_service.employee.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 직원 목록 조회 응답 DTO (MyBatis Mapping용)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeListResponse {
    private Long employeeCode;
    private Long employeeNumber;
    private String loginId;
    private byte[] employeeNameEnc;
    private byte[] phoneNumberEnc;
    private byte[] emailEnc;
    private byte[] dekEnc;
}
