package com.gaekdam.gaekdambe.iam_service.employee.command.application.service;

import com.gaekdam.gaekdambe.global.crypto.AesCryptoUtils;
import com.gaekdam.gaekdambe.global.crypto.DecryptionService;
import com.gaekdam.gaekdambe.global.crypto.KmsService;
import com.gaekdam.gaekdambe.global.crypto.PasswordValidator;
import com.gaekdam.gaekdambe.global.crypto.RandomPassword;
import com.gaekdam.gaekdambe.global.crypto.SearchHashService;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.global.smtp.MailSendService;
import com.gaekdam.gaekdambe.hotel_service.department.command.domain.entity.Department;
import com.gaekdam.gaekdambe.hotel_service.department.command.infrastructure.DepartmentRepository;
import com.gaekdam.gaekdambe.hotel_service.position.command.domain.entity.HotelPosition;
import com.gaekdam.gaekdambe.hotel_service.position.command.infrastructure.repository.HotelPositionRepository;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.dto.request.EmployeeUpdateSecureRequest;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.dto.request.PasswordChangeRequest;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.EmployeeStatus;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.AuditLog;
import com.gaekdam.gaekdambe.iam_service.log.command.application.service.AuditLogService;
import com.gaekdam.gaekdambe.iam_service.permission.command.domain.entity.Permission;
import com.gaekdam.gaekdambe.iam_service.permission.command.infrastructure.PermissionRepository;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
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
  private final AuditLogService auditLogService;
  private final MailSendService mailSendService;
  private final DecryptionService decryptionService;

  // @AuditLog 제거: 수동 로깅으로 전환 (Diff 저장)
  @Transactional
  public void updateEmployee(Long hotelGroupCode, Long employeeCode,
      EmployeeUpdateSecureRequest request,
      Employee accessor) {
    log.info("직원 정보 수정 시작 - employeeCode: {}, accessor: {}",
        employeeCode, accessor.getLoginId());
    if (!accessor.getHotelGroup().getHotelGroupCode().equals(hotelGroupCode)) {
      log.error("호텔 그룹 불일치 - employeeCode: {}, expected: {}, actual: {}",
          employeeCode, hotelGroupCode, accessor.getHotelGroup().getHotelGroupCode());
      throw new CustomException(ErrorCode.HOTEL_GROUP_CODE_NOT_MATCH);
    }

    Employee employee = employeeRepository.findById(employeeCode)
        .orElseThrow(() -> {
          log.error("직원을 찾을 수 없음 - employeeCode: {}", employeeCode);
          return new IllegalArgumentException("Employee not found: " + employeeCode);
        });

    // --- 변경 전 데이터 캡처 ---
    String prevDept =
        (employee.getDepartment() != null) ? employee.getDepartment().getDepartmentName() : "None";
    String prevPos =
        (employee.getHotelPosition() != null) ? employee.getHotelPosition().getHotelPositionName()
            : "None";
    String prevStatus = employee.getEmployeeStatus().name();
    String prevPermission =
        (employee.getPermission() != null) ? employee.getPermission().getPermissionName() : "None";
    Permission beforePermission = employee.getPermission();

    // 평문 DEK 복호화
    byte[] plaintextDek = kmsService.decryptDataKey(employee.getDekEnc());
    byte[] accessorPlaintextDek = kmsService.decryptDataKey(accessor.getDekEnc());

    // 전화번호 복호화 for Logging
    String prevPhone = "None";
    if (employee.getPhoneNumberEnc() != null) {
      prevPhone = AesCryptoUtils.decrypt(employee.getPhoneNumberEnc(), plaintextDek);
    }

    // 개인정보 업데이트
    updatePersonalInfo(employee, request, plaintextDek);

    // 권한 변경
    Permission afterPermission = (request.permissionCode() != null)
        ? permissionRepository.findById(request.permissionCode()).orElseThrow()
        : null;

    // 조직 정보 업데이트
    updateOrganizationInfo(employee, accessor, request, afterPermission, plaintextDek);

    // 상태 업데이트
    if (request.employeeStatus() != null) {
      log.debug("직원 상태 변경 - employeeCode: {}, {} -> {}",
          employeeCode, employee.getEmployeeStatus(), request.employeeStatus());
      employee.updateStatus(request.employeeStatus());
    }

    employeeRepository.save(employee);

    // --- 변경 후 데이터 캡처 & 비교 ---
    String newDept =
        (employee.getDepartment() != null) ? employee.getDepartment().getDepartmentName() : "None";
    String newPos =
        (employee.getHotelPosition() != null) ? employee.getHotelPosition().getHotelPositionName()
            : "None";
    String newStatus = employee.getEmployeeStatus().name();
    String newPermission =
        (employee.getPermission() != null) ? employee.getPermission().getPermissionName() : "None";
    String newPhone =
        (request.phoneNumber() != null) ? request.phoneNumber() : prevPhone; // 변경 안했으면 prev 그대로

    StringBuilder changes = new StringBuilder();
    StringBuilder prevVal = new StringBuilder();
    StringBuilder newVal = new StringBuilder();

    if (!prevDept.equals(newDept)) {
      changes.append(String.format("[부서 변경: %s -> %s] ", prevDept, newDept));
      prevVal.append(String.format("Dept: %s, ", prevDept));
      newVal.append(String.format("Dept: %s, ", newDept));
    }
    if (!prevPos.equals(newPos)) {
      changes.append(String.format("[직급 변경: %s -> %s] ", prevPos, newPos));
      prevVal.append(String.format("Pos: %s, ", prevPos));
      newVal.append(String.format("Pos: %s, ", newPos));
    }
    if (!prevStatus.equals(newStatus)) {
      changes.append(String.format("[상태 변경: %s -> %s] ", prevStatus, newStatus));
      prevVal.append(String.format("Status: %s, ", prevStatus));
      newVal.append(String.format("Status: %s, ", newStatus));
    }
    // 권한 변경 로깅 분리 (AuditLog -> PermissionChangedLog)
    if (afterPermission != null && !beforePermission.getPermissionCode()
        .equals(afterPermission.getPermissionCode())) {
      auditLogService.logPermissionChanged(employee, accessor, beforePermission, afterPermission);
    }
    // 전화번호 변경 로깅
    if (!prevPhone.equals(newPhone)) {
      changes.append(
          String.format("[전화번호 변경] ")); // 민감정보라 값은 details엔 안넣고 prev/new에만? 아니면 마스킹? 요구사항: "이전 이후 값
      // 저장되게"였으므로 저장.
      prevVal.append(String.format("Phone: %s, ", prevPhone));
      newVal.append(String.format("Phone: %s, ", newPhone));
    }

    // 변경사항이 없으면 "정보 수정" 정도로 기록하거나 생략 가능. 여기서는 기본 메시지 처리.
    String details = changes.length() > 0 ? changes.toString() : "직원 정보 수정 (개인정보 포함될 수 있음)";

    auditLogService.saveAuditLog(
        accessor,
        PermissionTypeKey.EMPLOYEE_UPDATE,
        details,
        (prevVal.length() > 0 ? prevVal.toString() : null),
        (newVal.length() > 0 ? newVal.toString() : null));

    log.info("직원 정보 수정 완료 - employeeCode: {}", employeeCode);
  }

  private void updatePersonalInfo(Employee employee, EmployeeUpdateSecureRequest request,
      byte[] plaintextDek) {
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

    if (phoneEnc != null || emailEnc != null) {
      log.debug("개인정보 업데이트 완료 - employeeCode: {}", employee.getEmployeeCode());
    }
  }

  private void updateOrganizationInfo(
      Employee employee,
      Employee accessor,
      EmployeeUpdateSecureRequest request,
      Permission afterPermission,
      byte[] plaintextDek) {

    Department department = (request.departmentCode() != null)
        ? departmentRepository.findById(request.departmentCode()).orElseThrow()
        : null;
    HotelPosition hotelPosition = (request.hotelPositionCode() != null)
        ? hotelPositionRepository.findById(request.hotelPositionCode()).orElseThrow()
        : null;

    // 기존의 auditLogService.logPermissionChanged 호출 제거 (상위 메서드에서 통합 로깅)

    employee.updateOrganization(department, hotelPosition, afterPermission);
  }

  @AuditLog(details = "'비밀번호 변경", type = PermissionTypeKey.EMPLOYEE_UPDATE)
  @Transactional
  public void changePassword(Employee employee, PasswordChangeRequest request) {
    log.info("비밀번호 변경 시작 - loginId: {}", employee.getLoginId());

    PasswordValidator.validate(request.newPassword());
    // 기존 비밀번호 검증
    if (!passwordEncoder.matches(request.currentPassword(), employee.getPasswordHash())) {
      log.warn("비밀번호 변경 실패: 현재 비밀번호 불일치 - loginId: {}", employee.getLoginId());
      throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
    } else if (passwordEncoder.matches(request.newPassword(), employee.getPasswordHash())) {
      log.warn("비밀번호 변경 실패: 새 비밀번호가 현재 비밀번호와 동일 - loginId: {}", employee.getLoginId());
      throw new IllegalArgumentException("새 비밀번호가 현재 비밀번호와 일치합니다.");
    }

    // 새 비밀번호 암호화 및 반영
    employee.changePassword(passwordEncoder.encode(request.newPassword()));
    employeeRepository.save(employee);

    log.info("비밀번호 변경 완료 - loginId: {}", employee.getLoginId());
  }

  @Transactional
  @AuditLog(details = "'비밀번호 초기화   직원 코드 : '+#employeeCode", type = PermissionTypeKey.EMPLOYEE_UPDATE)
  public String resetPassword(Long employeeCode) {
    log.info("비밀번호 초기화 시작 - employeeCode: {}", employeeCode);

    RandomPassword randomPassword = new RandomPassword();
    String tempPassword = randomPassword.getRandomPassword();
    Employee employee = employeeRepository.findById(employeeCode)
        .orElseThrow(() -> {
          log.error("직원을 찾을 수 없음 - employeeCode: {}", employeeCode);
          return new IllegalArgumentException("Employee not found: " + employeeCode);
        });

    // 암호화 및 초기화(잠금해제 포함)
    employee.resetToActive(passwordEncoder.encode(tempPassword));
    employeeRepository.save(employee);

    byte[] dekEnc = employee.getDekEnc();
    String email = decryptionService.decrypt(employeeCode, dekEnc, employee.getEmailEnc());

    mailSendService.resetPasswordEmail(email, tempPassword);

    log.info("비밀번호 초기화 완료 - employeeCode: {}, loginId: {}",
        employeeCode, employee.getLoginId());

    return tempPassword;
  }

  @Transactional
  @AuditLog(details = "'직원 잠금   직원 코드 : '+#employeeCode", type = PermissionTypeKey.EMPLOYEE_UPDATE)
  public void lockEmployee(Long hotelGroupCode, Long employeeCode) {
    log.info("직원 잠금 시작 - employeeCode: {}, hotelGroupCode: {}", employeeCode, hotelGroupCode);

    Employee employee = employeeRepository.findById(employeeCode).orElseThrow();
    if (!employee.getHotelGroup().getHotelGroupCode().equals(hotelGroupCode)) {
      log.error("호텔 그룹 불일치 - employeeCode: {}, expected: {}, actual: {}",
          employeeCode, hotelGroupCode, employee.getHotelGroup().getHotelGroupCode());
      throw new CustomException(ErrorCode.HOTEL_GROUP_CODE_NOT_MATCH);
    }
    if (employee.getEmployeeStatus() == EmployeeStatus.ACTIVE) {
      employee.employeeLocked();
      employeeRepository.save(employee);
      log.info("직원 잠금 완료 - employeeCode: {}, loginId: {}", employeeCode, employee.getLoginId());
    }
  }

  // 유저 잠금 해제
  @Transactional
  @AuditLog(details = "'직원 잠금해제  직원 코드 : '+#employeeCode", type = PermissionTypeKey.EMPLOYEE_UPDATE)
  public void unlockEmployee(Long hotelGroupCode, Long employeeCode) {
    log.info("직원 잠금 해제 시작 - employeeCode: {}, hotelGroupCode: {}", employeeCode, hotelGroupCode);

    Employee employee = employeeRepository.findById(employeeCode).orElseThrow();

    if (!employee.getHotelGroup().getHotelGroupCode().equals(hotelGroupCode)) {
      log.error("호텔 그룹 불일치 - employeeCode: {}, expected: {}, actual: {}",
          employeeCode, hotelGroupCode, employee.getHotelGroup().getHotelGroupCode());
      throw new CustomException(ErrorCode.HOTEL_GROUP_CODE_NOT_MATCH);
    }
    if (employee.getEmployeeStatus() != EmployeeStatus.ACTIVE) {
      employee.employeeUnlocked();
      employeeRepository.save(employee);
      log.info("직원 잠금 해제 완료 - employeeCode: {}, loginId: {}", employeeCode, employee.getLoginId());
    }
  }

  @Transactional
  @AuditLog(details = "'직원 비활성화   직원 코드 : '+#employeeCode", type = PermissionTypeKey.EMPLOYEE_UPDATE)
  public void inactiveEmployee(Long hotelGroupCode, Long employeeCode) {
    log.info("직원 비활성화 - employeeCode: {}, hotelGroupCode: {}", employeeCode, hotelGroupCode);

    Employee employee = employeeRepository.findById(employeeCode).orElseThrow();
    if (!employee.getHotelGroup().getHotelGroupCode().equals(hotelGroupCode)) {
      log.error("호텔 그룹 불일치 - employeeCode: {}, expected: {}, actual: {}",
          employeeCode, hotelGroupCode, employee.getHotelGroup().getHotelGroupCode());
      throw new CustomException(ErrorCode.HOTEL_GROUP_CODE_NOT_MATCH);
    }
    if (employee.getEmployeeStatus() == EmployeeStatus.ACTIVE) {
      employee.employeeInactive();
      employeeRepository.save(employee);
      log.info("직원 비활성화 완료 - employeeCode: {}, loginId: {}", employeeCode, employee.getLoginId());
    }
  }


  // 유저 휴면 전환
  @Scheduled(cron = "0 0 12 * * ?", zone = "Asia/Seoul")
  @Transactional
  public void dormancyEmployee() {
    log.info("휴면 계정 전환 스케줄러 실행");

    LocalDateTime targetDate = LocalDateTime.now().minusMonths(6);
    //LocalDateTime targetDate = LocalDateTime.now().minusMinutes(60);

    List<Employee> targetEmployees = employeeRepository
        .findByEmployeeStatusAndLastLoginAtBefore(EmployeeStatus.ACTIVE,
            targetDate);// ACTIVE이면서 마지막 로그인이 한달 전인 employee

    log.info("휴면 전환 대상: {} 명", targetEmployees.size());

    targetEmployees.forEach(Employee::employeeDormancy);
    employeeRepository.saveAll(targetEmployees);

    log.info("휴면 계정 전환 완료: {} 명", targetEmployees.size());
  }
}
