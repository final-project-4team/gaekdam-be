package com.gaekdam.gaekdambe.iam_service.auth.command.application.service;

import com.gaekdam.gaekdambe.iam_service.employee.command.domain.EmployeeStatus;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import com.gaekdam.gaekdambe.iam_service.log.command.application.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAuthService {
  private final EmployeeRepository employeeRepository;
  private final AuditLogService auditLogService;


  @Transactional
  public void loginFailed(Employee employee, String userIp,String failedReason) {
    log.debug("로그인 실패 처리 시작 - loginId: {}, ip: {}", employee.getLoginId(), userIp);

    employee.loginFailed();

    // 감사 로그 저장
    auditLogService.logLoginFailed(employee, userIp,failedReason);

    // 5회 이상 실패 시 계정 잠금
    if (employee.getFailedLoginCount() >= 5
        && employee.getEmployeeStatus() == EmployeeStatus.ACTIVE) {

      log.warn("로그인 5회 실패로 계정 잠금 - loginId: {}, ip: {}",
          employee.getLoginId(), userIp);

      employee.employeeLocked();

      // 계정 잠금 로그 저장
      auditLogService.logAccountLocked(employee, userIp);
    }

    // 변경 사항 저장
    employeeRepository.save(employee);

    log.debug("로그인 실패 처리 완료 - loginId: {}, failedCount: {}",
        employee.getLoginId(), employee.getFailedLoginCount());
  }


  @Transactional
  public void loginSuccess(Employee employee, String userIp) {
    log.debug("로그인 성공 처리 시작 - loginId: {}, ip: {}", employee.getLoginId(), userIp);

    employee.loginSuccess();

    // 감사 로그 저장
    auditLogService.logLoginSuccess(employee, userIp);

    employeeRepository.save(employee);

    log.info("로그인 성공 - loginId: {}, ip: {}, hotelGroup: {}",
        employee.getLoginId(),
        userIp,
        employee.getHotelGroup().getHotelGroupCode());
  }
}
