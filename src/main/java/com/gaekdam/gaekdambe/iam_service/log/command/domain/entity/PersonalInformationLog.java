package com.gaekdam.gaekdambe.iam_service.log.command.domain.entity;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.Customer;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "personal_information_log")
public class PersonalInformationLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "personal_information_log_code", nullable = false)
  private Long personalInformationLogCode;

  @Column(name = "occurred_at", nullable = false)
  private LocalDateTime occurredAt;

  @Column(name = "permission_type_key", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  private PermissionTypeKey permissionTypeKey;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_accessor_code", nullable = false)
  private Employee employeeAccessor;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "target_employee_code")
  private Employee targetEmployee;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "target_customer_code")
  private Customer targetCustomer;

  @Column(name = "purpose", nullable = false, length = 500)
  private String purpose;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "hotel_group_code", nullable = false)
  private HotelGroup hotelGroup;

  public static PersonalInformationLog createCustomerAccessLog(
      Employee accessor,
      Customer targetCustomer,
      PermissionTypeKey permissionTypeKey,
      String purpose) {

    return PersonalInformationLog.builder()
        .occurredAt(LocalDateTime.now())
        .permissionTypeKey(permissionTypeKey)
        .employeeAccessor(accessor)
        .targetEmployee(null)
        .targetCustomer(targetCustomer)
        .purpose(purpose)
        .hotelGroup(accessor.getHotelGroup())
        .build();
  }

  public static PersonalInformationLog createEmployeeAccessLog(
      Employee accessor,
      Employee targetEmployee,
      PermissionTypeKey permissionTypeKey,
      String purpose) {

    return PersonalInformationLog.builder()
        .occurredAt(LocalDateTime.now())
        .permissionTypeKey(permissionTypeKey)
        .employeeAccessor(accessor)
        .targetEmployee(targetEmployee)
        .targetCustomer(null)
        .purpose(purpose)
        .hotelGroup(accessor.getHotelGroup())
        .build();
  }
}