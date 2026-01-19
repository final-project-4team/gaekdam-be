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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor
@Table(name = "personal_information_log")
class PersonalInformationLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "personal_information_log_code", nullable = false)
  private Long personalInformationLogCode;

  @Column(name = "job_name", nullable = false, length = 50)
  private String jobName; // DELETE/UPDATE/CREATE/SELECT

  @Column(name = "occurred_at", nullable = false)
  private LocalDateTime occurredAt;

  @Column(name = "menu_name", nullable = false, length = 50)
  private String menuName; // User/Role/(접근 자료)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_code", nullable = false)
  private Employee employee;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "hotel_group_code", nullable = false)
  private HotelGroup hotelGroup;

 }