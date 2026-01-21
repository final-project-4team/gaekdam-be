package com.gaekdam.gaekdambe.iam_service.employee.command.application.service;

import com.gaekdam.gaekdambe.global.crypto.AesCryptoUtils;
import com.gaekdam.gaekdambe.global.crypto.DataKey;
import com.gaekdam.gaekdambe.global.crypto.KmsService;
import com.gaekdam.gaekdambe.global.crypto.SearchHashService;
import com.gaekdam.gaekdambe.hotel_service.department.command.domain.entity.Department;
import com.gaekdam.gaekdambe.hotel_service.department.command.infrastructure.DepartmentRepository;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.property.command.domain.entity.Property;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import com.gaekdam.gaekdambe.hotel_service.property.command.infrastructure.PropertyRepository;
import com.gaekdam.gaekdambe.hotel_service.position.command.domain.entity.HotelPosition;
import com.gaekdam.gaekdambe.hotel_service.position.command.infrastructure.repository.HotelPositionRepository;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.dto.request.EmployeeSecureRegistrationRequest;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import com.gaekdam.gaekdambe.iam_service.permission.command.domain.entity.Permission;
import com.gaekdam.gaekdambe.iam_service.permission.command.infrastructure.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// 직원 등록 시 보안 적용서비스
@Service
@RequiredArgsConstructor
public class EmployeeSecureRegistrationService {

  private final EmployeeRepository employeeRepository;
  private final KmsService kmsService;
  private final SearchHashService searchHashService;
  private final PasswordEncoder passwordEncoder;

  // 관련 엔티티 조회를 위한 레포지토리 추가
  private final DepartmentRepository departmentRepository;
  private final HotelPositionRepository hotelPositionRepository;
  private final PropertyRepository propertyRepository;
  private final HotelGroupRepository hotelGroupRepository;
  private final PermissionRepository permissionRepository;


  @Transactional
  public Long registerEmployee(EmployeeSecureRegistrationRequest command) {

    // 비밀번호 암호화 (BCrypt)
    String passwordToSave = passwordEncoder.encode(command.password());

    // KMS에서 데이터 키(DEK) 생성
    DataKey dek = kmsService.generateDataKey();

    // 민감 정보 AES-256 암호화 (Plaintext DEK 사용)
    // 이메일은 선택사항이므로 null 체크
    byte[] emailEnc = (command.email() != null)
        ? AesCryptoUtils.encrypt(command.email(), dek.plaintext())
        : null;
    byte[] phoneEnc = AesCryptoUtils.encrypt(command.phoneNumber(), dek.plaintext());
    byte[] nameEnc = AesCryptoUtils.encrypt(command.name(), dek.plaintext());

    // 검색용 해시 생성 (HMAC-SHA256)
    // 이메일은 선택사항이므로 null 체크
    byte[] emailHash = (command.email() != null)
        ? searchHashService.emailHash(command.email())
        : null;
    byte[] phoneHash = searchHashService.phoneHash(command.phoneNumber());
    byte[] nameHash = searchHashService.nameHash(command.name());

    // 연관 엔티티 조회
    Department department = departmentRepository.findById(command.departmentCode())
        .orElseThrow(() -> new IllegalArgumentException(
            "Department not found: " + command.departmentCode()));
    HotelPosition position = hotelPositionRepository.findById(command.positionCode())
        .orElseThrow(
            () -> new IllegalArgumentException("Position not found: " + command.positionCode()));
    Property property = propertyRepository.findById(command.propertyCode())
        .orElseThrow(
            () -> new IllegalArgumentException("Property not found: " + command.propertyCode()));
    HotelGroup hotelGroup = hotelGroupRepository.findById(command.hotelGroupCode())
        .orElseThrow(() -> new IllegalArgumentException(
            "HotelGroup not found: " + command.hotelGroupCode()));
    Permission role = permissionRepository.findById(command.permissionCode())
        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + command.permissionCode()));

    Employee employee = Employee.createEmployee(
        command.employeeNumber(),
        command.loginId(),
        passwordToSave,
        emailEnc,
        phoneEnc,
        nameEnc,
        emailHash,
        phoneHash,
        nameHash,
        dek.encrypted(),
        LocalDateTime.now(),
        department,
        position,
        property,
        hotelGroup,
        role);

    Employee saved = employeeRepository.save(employee);

    return saved.getEmployeeCode();
  }

}
