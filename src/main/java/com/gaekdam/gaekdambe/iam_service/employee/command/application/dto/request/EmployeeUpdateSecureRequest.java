package com.gaekdam.gaekdambe.iam_service.employee.command.application.dto.request;

import com.gaekdam.gaekdambe.iam_service.employee.command.domain.EmployeeStatus;
import jakarta.validation.constraints.Pattern;

public record   EmployeeUpdateSecureRequest(
        //정규식 설정
        @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다.")
        String phoneNumber,

        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "이메일 형식이 올바르지 않습니다.")
        String email,
        Long departmentCode,
        Long hotelPositionCode,
        Long permissionCode,
        EmployeeStatus employeeStatus) {
}
