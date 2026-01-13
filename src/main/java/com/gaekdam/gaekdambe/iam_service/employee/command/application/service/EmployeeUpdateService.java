package com.gaekdam.gaekdambe.iam_service.employee.command.application.service;

import com.gaekdam.gaekdambe.global.crypto.AesCryptoUtils;
import com.gaekdam.gaekdambe.global.crypto.KmsService;
import com.gaekdam.gaekdambe.global.crypto.RandomPassword;
import com.gaekdam.gaekdambe.global.crypto.SearchHashService;
import com.gaekdam.gaekdambe.hotel_service.department.command.domain.entity.Department;
import com.gaekdam.gaekdambe.hotel_service.department.command.infrastructure.DepartmentRepository;
import com.gaekdam.gaekdambe.hotel_service.position.command.domain.entity.HotelPosition;
import com.gaekdam.gaekdambe.hotel_service.position.command.infrastructure.repository.HotelPositionRepository;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.dto.request.EmployeeUpdateSecureRequest;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.dto.request.PasswordChangeRequest;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.EmployeeStatus;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import com.gaekdam.gaekdambe.iam_service.permission.command.domain.entity.Permission;
import com.gaekdam.gaekdambe.iam_service.permission.command.infrastructure.PermissionRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeUpdateService {

  private final EmployeeRepository employeeRepository;
  private final KmsService kmsService;
  private final SearchHashService searchHashService;

  private final DepartmentRepository departmentRepository;
  private final HotelPositionRepository hotelPositionRepository;
  private final PermissionRepository permissionRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public void updateEmployee(Long employeeCode, EmployeeUpdateSecureRequest request) {
    Employee employee = employeeRepository.findById(employeeCode)
        .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeCode));

    // 평문 DEK 복호화
    byte[] plaintextDek = kmsService.decryptDataKey(employee.getDekEnc());

    // 개인정보 업데이트
    byte[] phoneEnc =
        (request.phoneNumber() != null) ? AesCryptoUtils.encrypt(request.phoneNumber(),
            plaintextDek)
            : null;
    byte[] phoneHash =
        (request.phoneNumber() != null) ? searchHashService.phoneHash(request.phoneNumber()) : null;

    byte[] emailEnc =
        (request.email() != null) ? AesCryptoUtils.encrypt(request.email(), plaintextDek) : null;
    byte[] emailHash =
        (request.email() != null) ? searchHashService.emailHash(request.email()) : null;

    employee.updatePersonalInfo(phoneEnc, phoneHash, emailEnc, emailHash);

    // 조직 정보 업데이트
    Department department = (request.departmentCode() != null)
        ? departmentRepository.findById(request.departmentCode()).orElseThrow()
        : null;
    HotelPosition hotelPosition = (request.hotelPositionCode() != null)
        ? hotelPositionRepository.findById(request.hotelPositionCode()).orElseThrow()
        : null;
    Permission permission = (request.permissionCode() != null)
        ? permissionRepository.findById(request.permissionCode()).orElseThrow()
        : null;

    employee.updateOrganization(department, hotelPosition, permission);

    // 상태 업데이트
    employee.updateStatus(request.employeeStatus());

    employeeRepository.save(employee);
  }

  @Transactional
  public void changePassword(Employee employee, PasswordChangeRequest request) {
    // 기존 비밀번호 검증
    if (!passwordEncoder.matches(request.currentPassword(), employee.getPasswordHash())) {
      throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
    } else if (passwordEncoder.matches(request.newPassword(), employee.getPasswordHash())) {
      throw new IllegalArgumentException("새 비밀번호가 현재 비밀번호와 일치합니다.");
    }

    // 새 비밀번호 암호화 및 반영
    employee.changePassword(passwordEncoder.encode(request.newPassword()));
    employeeRepository.save(employee);
  }

  @Transactional
  public String resetPassword(Long employeeCode) {
    RandomPassword randomPassword = new RandomPassword();
    String tempPassword = randomPassword.getRandomPassword();
    Employee employee = employeeRepository.findById(employeeCode)
        .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeCode));

    // 암호화 및 초기화(잠금해제 포함)
    employee.resetToActive(passwordEncoder.encode(tempPassword));
    employeeRepository.save(employee);
    return tempPassword;
  }

  @Transactional
  public void unlockEmployee(Long employeeCode) {
    Employee employee = employeeRepository.findById(employeeCode).orElseThrow();
    if (employee.getEmployeeStatus() == EmployeeStatus.LOCKED
        || employee.getEmployeeStatus() == EmployeeStatus.DORMANCY) {
      employee.employeeUnlocked();
      employeeRepository.save(employee);
    }
  }

  //유저 휴면 전환
  @Scheduled(cron = "0 0 12 * * ?")
  @Transactional
  public void dormancyEmployee() {
    LocalDateTime targetDate = LocalDateTime.now().minusMonths(6);

    List<Employee> targetEmployees = employeeRepository
        .findByEmployeeStatusAndLastLoginAtBefore(EmployeeStatus.ACTIVE,
            targetDate);//ACTIVE이면서 마지막 로그인이 한달 전인 employee
    targetEmployees.forEach(Employee::employeeDormancy);
  }
}
