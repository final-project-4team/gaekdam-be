package com.gaekdam.gaekdambe.iam_service.log.command.domain.entity;

import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "login_log")
class LoginLog {

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
  private Integer userIp; // DDL: INT (IPv4 int 저장 가정)

  @Column(name = "occurred_at", nullable = false)
  private LocalDateTime occurredAt;

  @Column(name = "result", nullable = false, length = 2)
  private String result; // 'Y' / 'N'

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "hotel_group_code", nullable = false)
  private HotelGroup hotelGroup;

  @Lob
  @Column(name = "failed_reason")
  private String failedReason;

}
