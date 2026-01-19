package com.gaekdam.gaekdambe.iam_service.log.command.domain.entity;

import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "audit_log")
public class AuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "audit_log_code", nullable = false)
  private Long auditLogCode;

  @Column(name = "job_name", nullable = false, length = 50)
  private String jobName; // DELETE/UPDATE/CREATE/SELECT

  @Column(name = "occurred_at", nullable = false)
  private LocalDateTime occurredAt;

  @Column(name = "menu_name", nullable = false, length = 50)
  private String menuName;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_code", nullable = false)
  private Employee employee;

  @Lob
  @Column(name = "details")
  private String details;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "hotel_group_code", nullable = false)
  private HotelGroup hotelGroup;


 }


