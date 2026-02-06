package com.gaekdam.gaekdambe.iam_service.log.command.domain.entity;

import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "audit_log")
public class AuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "audit_log_code", nullable = false, updatable = false)
  private Long auditLogCode;

  @Column(name = "permission_type_key", nullable = false, length = 50, updatable = false)
  @Enumerated(EnumType.STRING)
  private PermissionTypeKey permissionTypeKey;

  @CreatedDate
  @Column(name = "occurred_at", nullable = false, updatable = false)
  private LocalDateTime occurredAt;

  @Column(name = "employee_code", nullable = false, updatable = false)
  private Long employeeCode;

  @Column(name = "employee_login_id", nullable = false, length = 50, updatable = false)
  private String employeeLoginId;

  @Column(name = "employee_name", length = 100, updatable = false)
  private String employeeName; // 당시 이름 스냅샷

  @Column(name = "hotel_group_code", nullable = false, updatable = false)
  private Long hotelGroupCode; // HotelGroup FK 제거 -> ID 저장

  @Lob
  @Column(name = "details",columnDefinition = "MEDIUMTEXT", updatable = false)
  private String details;

  @Lob
  @Column(name = "previous_value",columnDefinition = "MEDIUMTEXT")
  private String previousValue;

  @Lob
  @Column(name = "new_value",columnDefinition = "MEDIUMTEXT")
  private String newValue;

  @Builder
  public AuditLog(
      PermissionTypeKey permissionTypeKey,
      Long employeeCode,
      String employeeLoginId,
      String employeeName,
      Long hotelGroupCode,
      String details,
      String previousValue,
      String newValue) {
    this.permissionTypeKey = permissionTypeKey;
    this.employeeCode = employeeCode;
    this.employeeLoginId = employeeLoginId;
    this.employeeName = employeeName;
    this.hotelGroupCode = hotelGroupCode;
    this.details = details;
    this.previousValue = previousValue;
    this.newValue = newValue;
    this.occurredAt = LocalDateTime.now();
  }

  // 생성 팩토리 메서드
  public static AuditLog createLog(
      Employee employee,
      PermissionTypeKey type){
    return AuditLog.builder()
        .permissionTypeKey(type)
        .employeeCode(employee.getEmployeeCode())
        .employeeLoginId(employee.getLoginId())
        .employeeName(employee.getLoginId())
        .hotelGroupCode(employee.getHotelGroup().getHotelGroupCode())
        .build();
  }


  public static AuditLog createLog(
      Employee employee,
      PermissionTypeKey type,
      String details,
      String previousValue,
      String newValue) {

    return AuditLog.builder()
        .permissionTypeKey(type)
        .employeeCode(employee.getEmployeeCode())
        .employeeLoginId(employee.getLoginId())
        .employeeName(employee.getLoginId())
        .hotelGroupCode(employee.getHotelGroup().getHotelGroupCode())
        .details(details)
        .previousValue(previousValue)
        .newValue(newValue)
        .build();
  }
}
