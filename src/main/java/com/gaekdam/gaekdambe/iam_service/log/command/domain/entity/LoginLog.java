package com.gaekdam.gaekdambe.iam_service.log.command.domain.entity;

import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.log.command.domain.LoginResult;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class) // 엔티티의 변화를 감지
// AuditingEntityListener : 엔티티의 영속, 수정 이벤트를 감지
@Table(name = "login_log")
public class LoginLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "login_log_code", nullable = false)
  private Long loginLogCode;

  @Column(name = "action", nullable = false, length = 50)
  private String action;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_code", nullable = false)
  private Employee employee;

  @Column(name = "user_ip", nullable = false)
  private String userIp; // DDL: INT (IPv4 int 저장 가정)

  @Column(name = "occurred_at", nullable = false)
  private LocalDateTime occurredAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "result", nullable = false, length = 2)
  private LoginResult result;

  @Column(name = "failed_reason")
  private String failedReason;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "hotel_group_code", nullable = false)
  private HotelGroup hotelGroup;

  public static LoginLog createLoginLog(
      String action,
      Employee employee,
      String userIp,
      LocalDateTime occurredAt,
      LoginResult result,
      String failedReason,
      HotelGroup hotelGroup) {
    return LoginLog.builder()
        .action(action)
        .employee(employee)
        .userIp(userIp)
        .occurredAt(occurredAt)
        .result(result)
        .failedReason(failedReason)
        .hotelGroup(hotelGroup)
        .build();
  }

}
