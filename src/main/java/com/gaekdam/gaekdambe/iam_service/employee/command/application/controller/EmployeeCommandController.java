
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/employee")
public class EmployeeCommandController {
  private final EmployeeRepository employeeRepository;
  private final EmployeeSecureRegistrationService employeeSecureRegistrationService;
  private final EmployeeUpdateService employeeUpdateService;
  //직원 추가
  @PostMapping("/add")
  @PreAuthorize("hasAuthority('EMPLOYEE_CREATE')")
  public ResponseEntity<ApiResponse<String>> registerEmployee(@RequestBody EmployeeSecureRegistrationRequest request) {

    employeeSecureRegistrationService.registerEmployee(request);

    return ResponseEntity.ok(ApiResponse.success("유저 추가"));
  }



  //직원 정보 수정
  @PutMapping("/{employeeCode}")
  public ResponseEntity<ApiResponse<String>> updateEmployee(
      @PathVariable Long employeeCode,
      //정규식 검사
      @Valid @RequestBody EmployeeUpdateSecureRequest request) {
    employeeUpdateService.updateEmployee(employeeCode, request);
    return ResponseEntity.ok(ApiResponse.success("유저 정보 수정 완료"));
  }

  //직원 본인 비밀번호 변경


  @PatchMapping("/password")
  public ResponseEntity<ApiResponse<String>> changePassword(
      @AuthenticationPrincipal CustomUser customUser,
      @RequestBody PasswordChangeRequest request) {
    Employee employee =employeeRepository.findByLoginId(customUser.getUsername()).orElseThrow();
    employeeUpdateService.changePassword(employee, request);
    return ResponseEntity.ok(ApiResponse.success("비밀번호가 성공적으로 변경 되었습니다." ));
  }
  //직원 잠금 헤제
  @PatchMapping("/unlock/{employeeCode}")
  public ResponseEntity<ApiResponse<String>> unlockEmployee(
      @AuthenticationPrincipal CustomUser customUser,
      @PathVariable Long employeeCode
     ) {
    Employee employee =employeeRepository.findByLoginId(customUser.getUsername()).orElseThrow();
    employeeUpdateService.unlockEmployee(employeeCode);
    return ResponseEntity.ok(ApiResponse.success("잠금 상태가 해제 되었습니다" ));
  }

  //직원 비밀번호 초기화
  @PatchMapping("/password-reset/{employeeCode}")
  public ResponseEntity<ApiResponse<String>> resetPassword(
      @PathVariable Long employeeCode) {
    String tempPassword=employeeUpdateService.resetPassword(employeeCode);
    return ResponseEntity.ok(ApiResponse.success("비밀번호가 성공적으로 초기화 되었습니다. 임시 비밀번호는 : "+tempPassword));
  }


}
