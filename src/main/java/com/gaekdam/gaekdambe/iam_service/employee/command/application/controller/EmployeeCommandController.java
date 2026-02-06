
package com.gaekdam.gaekdambe.iam_service.employee.command.application.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.dto.request.EmployeeSecureRegistrationRequest;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.dto.request.EmployeeUpdateSecureRequest;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.dto.request.PasswordChangeRequest;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.service.EmployeeSecureRegistrationService;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.service.EmployeeUpdateService;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name="직원")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/employee")
public class EmployeeCommandController {
  private final EmployeeRepository employeeRepository;
  private final EmployeeSecureRegistrationService employeeSecureRegistrationService;
  private final EmployeeUpdateService employeeUpdateService;

  // 직원 추가
  @PostMapping("/add")
  @PreAuthorize("hasAuthority('EMPLOYEE_CREATE')")
  @Operation(summary = "직원 생성", description = "새로운 직원을 생성 합니다.")
  public ResponseEntity<ApiResponse<String>> registerEmployee(
      @Valid @RequestBody EmployeeSecureRegistrationRequest request,
      @AuthenticationPrincipal CustomUser customUser
      ) {
    Long hotelGroupCode=customUser.getHotelGroupCode();
    employeeSecureRegistrationService.registerEmployee(hotelGroupCode,request);

    return ResponseEntity.ok(ApiResponse.success("유저 추가"));
  }

  // 직원 정보 수정
  @PutMapping("/{employeeCode}")
  @PreAuthorize("hasAuthority('EMPLOYEE_UPDATE')")
  @Operation(summary = "직원 정보 수정", description = "특정 직원의 정보를 수정 합니다.")
  public ResponseEntity<ApiResponse<String>> updateEmployee(
      @AuthenticationPrincipal CustomUser customUser,
      @Parameter(name="직원 코드") @PathVariable Long employeeCode,
      @Valid @RequestBody EmployeeUpdateSecureRequest request) {
    Long hotelGroupCode= customUser.getHotelGroupCode();
    Employee accessor = employeeRepository.findByLoginId(customUser.getUsername()).orElseThrow();
    employeeUpdateService.updateEmployee(hotelGroupCode,employeeCode, request, accessor);
    return ResponseEntity.ok(ApiResponse.success("유저 정보 수정 완료"));
  }

  // 직원 본인 비밀번호 변경

  @PatchMapping("/password")
  @Operation(summary = "직원 비밀번호 변경", description = "특정 직원의 비밀번호를 변경 할 수 있다.")
  public ResponseEntity<ApiResponse<String>> changePassword(
      @AuthenticationPrincipal CustomUser customUser,
      @RequestBody PasswordChangeRequest request) {
    Employee employee = employeeRepository.findByLoginId(customUser.getUsername()).orElseThrow();
    employeeUpdateService.changePassword(employee, request);
    return ResponseEntity.ok(ApiResponse.success("비밀번호가 성공적으로 변경 되었습니다."));
  }

  // 직원 잠금
  @PatchMapping("/lock/{employeeCode}")
  @PreAuthorize("hasAuthority('EMPLOYEE_UPDATE')")
  @Operation(summary = "직원 잠금 처리", description = "특정 직원을 잠금 처리 합니다.")
  public ResponseEntity<ApiResponse<String>> lockEmployee(
      @AuthenticationPrincipal CustomUser employee,
      @Parameter(description = "직원 코드") @PathVariable Long employeeCode) {
    Long hotelGroupCode = employee.getHotelGroupCode();
    employeeUpdateService.lockEmployee(hotelGroupCode, employeeCode);
    return ResponseEntity.ok(ApiResponse.success("잠금 되었습니다"));
  }

  // 직원 잠금 헤제
  @PatchMapping("/unlock/{employeeCode}")
  @PreAuthorize("hasAuthority('EMPLOYEE_UPDATE')")
  @Operation(summary = "직원 잠금 해제 처리", description = "특정 직원을 잠금 해제 합니다.")
  public ResponseEntity<ApiResponse<String>> unlockEmployee(
      @AuthenticationPrincipal CustomUser employee,
      @Parameter(description = "직원 코드") @PathVariable Long employeeCode) {
    Long hotelGroupCode = employee.getHotelGroupCode();
    employeeUpdateService.unlockEmployee(hotelGroupCode, employeeCode);
    return ResponseEntity.ok(ApiResponse.success("잠금 상태가 해제 되었습니다"));
  }

  // 직원 비밀번호 초기화
  @PatchMapping("/password-reset/{employeeCode}")
  @PreAuthorize("hasAuthority('EMPLOYEE_UPDATE')")
  @Operation(summary = "직원 비밀번호 초기화", description = "특정 직원의 비밀번호를 초기화 합니다.")
  public ResponseEntity<ApiResponse<String>> resetPassword(
      @Parameter(description = "직원 코드")  @PathVariable Long employeeCode) {
    String tempPassword = employeeUpdateService.resetPassword(employeeCode);
    return ResponseEntity.ok(ApiResponse.success("비밀번호가 성공적으로 초기화 되었습니다.\n 임시 비밀번호는 : " + tempPassword));
  }

  @PatchMapping("/inactive/{employeeCode}")
  @PreAuthorize("hasAuthority('EMPLOYEE_UPDATE')")
  @Operation(summary = "직원 비활성화 처리", description = "특정 직원을 비활성화 처리 합니다.")
  public ResponseEntity<ApiResponse<String>> inactiveEmployee(
      @AuthenticationPrincipal CustomUser employee,
      @Parameter(description = "직원 코드") @PathVariable Long employeeCode) {
    Long hotelGroupCode = employee.getHotelGroupCode();
    employeeUpdateService.inactiveEmployee(hotelGroupCode, employeeCode);
    return ResponseEntity.ok(ApiResponse.success("비활성화 처리 되었습니다."));
  }

}
