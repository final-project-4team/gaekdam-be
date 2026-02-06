package com.gaekdam.gaekdambe.iam_service.log.command.domain.entity;

import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.permission.command.domain.entity.Permission;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Table(name = "permission_changed_log")
public class PermissionChangedLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "permission_changed_log_code", nullable = false)
  private Long permissionChangedLogCode;

  @Column(name = "changed_at", nullable = false)
  private LocalDateTime changedAt;



  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "accessor_code", nullable = false)
  private Employee employeeAccessor;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "hotel_group_code", nullable = false)
  private HotelGroup hotelGroup;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "before_permission_code", nullable = false)
  private Permission beforePermission;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "after_permission_code", nullable = false)
  private Permission afterPermission;


  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "changed_user_code", nullable = false)
  private Employee employeeChanged;


  public static PermissionChangedLog createPermissionChangedLog(
      LocalDateTime changedAt,
      Employee employeeAccessor,
      HotelGroup hotelGroup,
      Permission beforePermission,
      Permission afterPermission,
      Employee employeeChanged
  ) {
    return PermissionChangedLog.builder()
        .changedAt(changedAt)
        .employeeAccessor(employeeAccessor)
        .hotelGroup(hotelGroup)
        .beforePermission(beforePermission)
        .afterPermission(afterPermission)
        .employeeChanged(employeeChanged).build();
  }
}
