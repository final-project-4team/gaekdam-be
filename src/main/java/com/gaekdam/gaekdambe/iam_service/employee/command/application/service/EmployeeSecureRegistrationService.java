package com.gaekdam.gaekdambe.iam_service.employee.command.application.service;

import com.gaekdam.gaekdambe.global.crypto.AesCryptoUtils;
import com.gaekdam.gaekdambe.global.crypto.DataKey;
import com.gaekdam.gaekdambe.global.crypto.KmsService;
import com.gaekdam.gaekdambe.global.crypto.SearchHashService;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import lombok.RequiredArgsConstructor;
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

  public record RegisterEmployeeCommand(
      Long employeeNumber,
      String loginId,
      String password,
      String email,
      String phoneNumber,
      String name,
      Long departmentCode,
      Long positionCode,
      Long propertyCode,
      Long hotelGroupCode,
      Long roleCode) {
  }

 // KMS를 통한 데이터 암호화 키(DEK) 생성
 // 개인정보(이름, 전화번호, 이메일) AES-256 암호화
 // 검색용 해시 생성 (HMAC-SHA256)
 //DB 저장 (Envelope Encryption 적용)
  @Transactional
  public Long registerEmployee(RegisterEmployeeCommand command) {

    // 사용자 요청에 따라 암호화 로직 제거 (추후 JWT/Spring Security 적용 예정)
    String passwordToSave = command.password();

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
        command.departmentCode(),
        command.positionCode(),
        command.propertyCode(),
        command.hotelGroupCode(),
        command.roleCode()
    );

    Employee saved = employeeRepository.save(employee);

    return saved.getEmployeeCode();
  }
}
