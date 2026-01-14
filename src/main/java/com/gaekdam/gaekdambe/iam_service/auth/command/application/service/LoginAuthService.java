package com.gaekdam.gaekdambe.iam_service.auth.command.application.service;

import com.gaekdam.gaekdambe.iam_service.employee.command.domain.EmployeeStatus;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginAuthService {
  private final EmployeeRepository employeeRepository;



  @Transactional
  public void loginFailed(Employee employee) {

    employee.loginFailed();

    // 증가된 횟수가 5회 이상이면 즉시 잠금 처리
    if (employee.getFailedLoginCount() >= 5
        && employee.getEmployeeStatus() == EmployeeStatus.ACTIVE) {
      employee.employeeLocked();
    }

    // 변경 사항 저장
    employeeRepository.save(employee);
  }

  @Transactional
  public void loginSuccess(Employee employee) {
    employee.loginSuccess();
    employeeRepository.save(employee);
  }
}
