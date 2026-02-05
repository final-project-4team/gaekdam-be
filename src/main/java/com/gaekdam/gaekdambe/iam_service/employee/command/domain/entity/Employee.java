package com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity;

import com.gaekdam.gaekdambe.hotel_service.department.command.domain.entity.Department;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.property.command.domain.entity.Property;
import com.gaekdam.gaekdambe.hotel_service.position.command.domain.entity.HotelPosition;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.EmployeeStatus;
import com.gaekdam.gaekdambe.iam_service.permission.command.domain.entity.Permission;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.ToString;

@Entity
@Table(name = "employee")
@Getter
@ToString
public class Employee {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "employee_code")
  private Long employeeCode;

  @Column(name = "employee_number", nullable = false)
  private Long employeeNumber;

  @Column(name = "login_id", nullable = false, unique = true)
  private String loginId;

  @Lob
  @Column(name = "email_enc",nullable = false)
  private byte[] emailEnc;

  @Column(name = "email_hash",nullable = false)
  private byte[] emailHash;

  @Lob
  @Column(name = "phone_number_enc", nullable = false)
  private byte[] phoneNumberEnc;

  @Column(name = "phone_number_hash", nullable = false)
  private byte[] phoneNumberHash;

  @Lob
  @Column(name = "employee_name_enc", nullable = false)
  private byte[] employeeNameEnc;

  @Column(name = "employee_name_hash", nullable = false)
  private byte[] employeeNameHash;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "hired_at", nullable = false)
  private LocalDateTime hiredAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "department_code", nullable = false)
  private Department department;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "hotel_position_code", nullable = false)
  private HotelPosition hotelPosition;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "property_code")
  private Property property;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "hotel_group_code", nullable = false)
  private HotelGroup hotelGroup;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "permission_code", nullable = false)
  private Permission permission;

  @Enumerated(EnumType.STRING)
  @Column(name = "employee_status", nullable = false)
  private EmployeeStatus employeeStatus;

  @Column(name = "failed_login_count", nullable = false)
  private int failedLoginCount;

  @Column(name = "last_login_at")
  private LocalDateTime lastLoginAt;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "kms_key_id")
  private String kmsKeyId;

  @Lob
  @Column(name = "dek_enc", nullable = false)
  private byte[] dekEnc;

  public static Employee createEmployee(
      Long employeeNumber,
      String loginId,
      String passwordHash,
      byte[] emailEnc,
      byte[] phoneNumberEnc,
      byte[] employeeNameEnc,
      byte[] emailHash,
      byte[] phoneNumberHash,
      byte[] employeeNameHash,
      byte[] dekEnc,
      LocalDateTime hiredAt,
      Department department,
      HotelPosition hotelPosition,
      Property property,
      HotelGroup hotelGroup,
      Permission permission) {

    if (employeeNumber == null)
      throw new IllegalArgumentException("employeeNumber is required");
    if (loginId == null || loginId.isBlank())
      throw new IllegalArgumentException("loginId is required");
    if (passwordHash == null || passwordHash.isBlank())
      throw new IllegalArgumentException("passwordHash is required");
    if (phoneNumberHash == null)
      throw new IllegalArgumentException("phoneNumberHash must be 32 bytes");
    if (employeeNameHash == null)
      throw new IllegalArgumentException("employeeNameHash must be 32 bytes");
    if (dekEnc == null)
      throw new IllegalArgumentException("DEK (Encrypted) is required for secure storage");
    if (employeeNameEnc == null)
      throw new IllegalArgumentException("Employee name must be encrypted");
    if (phoneNumberEnc == null)
      throw new IllegalArgumentException("Phone number must be encrypted");
    if (hiredAt == null)
      throw new IllegalArgumentException("hiredAt is required");

    Employee e = new Employee();
    e.employeeNumber = employeeNumber;
    e.loginId = loginId;
    e.passwordHash = passwordHash;
    e.emailEnc = emailEnc;
    e.phoneNumberEnc = phoneNumberEnc;
    e.employeeNameEnc = employeeNameEnc;
    e.emailHash = emailHash;
    e.phoneNumberHash = phoneNumberHash;
    e.employeeNameHash = employeeNameHash;
    e.dekEnc = dekEnc;
    e.hiredAt = hiredAt;
    e.department = department;
    e.hotelPosition = hotelPosition;
    e.property = property;
    e.hotelGroup = hotelGroup;
    e.permission = permission;
    e.createdAt = LocalDateTime.now();
    e.updatedAt = LocalDateTime.now();
    e.employeeStatus = EmployeeStatus.ACTIVE;
    e.failedLoginCount = 0;

    return e;
  }

  // 개인정보 업데이트
  public void updatePersonalInfo(
      byte[] phoneEnc, byte[] phoneHash,
      byte[] emailEnc, byte[] emailHash) {
    if (phoneEnc != null)
      this.phoneNumberEnc = phoneEnc;
    if (phoneHash != null)
      this.phoneNumberHash = phoneHash;
    if (emailEnc != null)
      this.emailEnc = emailEnc;
    if (emailHash != null)
      this.emailHash = emailHash;
    this.updatedAt = LocalDateTime.now();
  }

  // 조직 정보 업데이트
  public void updateOrganization(
      Department department,
      HotelPosition hotelPosition,
      Permission permission) {
    if (department != null)
      this.department = department;
    if (hotelPosition != null)
      this.hotelPosition = hotelPosition;
    if (permission != null)
      this.permission = permission;
    this.updatedAt = LocalDateTime.now();
  }

  // 상태 업데이트
  public void updateStatus(EmployeeStatus status) {
    if (status != null) {
      this.employeeStatus = status;
      this.updatedAt = LocalDateTime.now();
    }
  }

  // 직원 상태 잠김
  public void employeeLocked() {
    this.employeeStatus = EmployeeStatus.LOCKED;
  }

  // 유저 상태 활성화
  public void employeeUnlocked() {
    this.employeeStatus = EmployeeStatus.ACTIVE;
    this.failedLoginCount=0;
  }

  // 유저 휴면으로 변경
  public void employeeDormancy() {
    this.employeeStatus = EmployeeStatus.DORMANCY;
  }

  // 로그인 성공 시 처리
  public void loginSuccess() {
    this.failedLoginCount = 0;
    this.lastLoginAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  // 비밀번호 변경 및 실패 횟수 초기화
  public void changePassword(String newPasswordHash) {
    this.passwordHash = newPasswordHash;
    this.failedLoginCount = 0;
    this.updatedAt = LocalDateTime.now();
  }

  // 관리자에 의한 계정 잠금 해제 및 비밀번호 초기화
  public void resetToActive(String temporaryPasswordHash) {
    this.passwordHash = temporaryPasswordHash;
    this.failedLoginCount = 0;
    this.employeeStatus = EmployeeStatus.ACTIVE;
    this.updatedAt = LocalDateTime.now();
  }

  // 로그인 실패 시 처리
  public void loginFailed() {
    this.failedLoginCount++;
    this.updatedAt = LocalDateTime.now();
  }
  public void employeeInactive() {
    this.employeeStatus = EmployeeStatus.INACTIVE;
  }

}
