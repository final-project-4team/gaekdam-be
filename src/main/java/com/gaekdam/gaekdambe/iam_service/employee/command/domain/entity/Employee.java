package com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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

  @Column(name = "employee_number", nullable = false, unique = true)
  private Long employeeNumber;

  @Column(name = "login_id", nullable = false, unique = true)
  private String loginId;

  @Lob
  @Column(name = "email_enc")
  private byte[] emailEnc;

  @Column(name = "email_hash")
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

  @Column(name = "department_code")
  private Long departmentCode;

  @Column(name = "position_code")
  private Long positionCode;

  @Column(name = "property_code")
  private Long propertyCode;

  @Column(name = "hotel_group_code")
  private Long hotelGroupCode;


  @Column(name = "role_code")
  private Long roleCode;

  @Column(name = "is_active", nullable = false)
  private boolean isActive = true;

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
      Long departmentCode,
      Long positionCode,
      Long propertyCode,
      Long hotelGroupCode,
      Long roleCode) {

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
    e.departmentCode = departmentCode;
    e.positionCode = positionCode;
    e.propertyCode = propertyCode;
    e.hotelGroupCode = hotelGroupCode;
    e.roleCode = roleCode;
    e.createdAt = LocalDateTime.now();
    e.updatedAt = LocalDateTime.now();
    e.isActive = true;

    return e;
  }

}
