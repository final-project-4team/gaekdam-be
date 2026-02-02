package com.gaekdam.gaekdambe.iam_service.employee.command.application.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record EmployeeSecureRegistrationRequest (
    @NotNull(message = "사원 번호는 필수입니다.")
    Long employeeNumber,

    @NotBlank(message = "로그인 ID는 필수입니다.")
    String loginId,

    @NotBlank(message = "비밀번호는 필수입니다.")
    String password,

    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "이메일 형식이 올바르지 않습니다.")
    String email,

    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다.")
    String phoneNumber,

    @NotBlank(message = "이름은 필수입니다.")
    String name,

    Long departmentCode,
    Long positionCode,
    Long propertyCode,
    Long permissionCode
){
}