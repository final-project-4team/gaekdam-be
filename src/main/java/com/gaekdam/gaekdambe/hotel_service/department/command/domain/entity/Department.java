package com.gaekdam.gaekdambe.hotel_service.department.command.domain.entity;

import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="department")
public class Department {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="department_code")
  private Long departmentCode;

  @Column(name="department_name",nullable = false)
  private String departmentName;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "hotel_group_code",nullable = false)
  private HotelGroup hotelGroup;

  public static Department createDepartment(String departmentName,HotelGroup hotelGroup) {
    return Department.builder()
        .departmentName(departmentName)
        .hotelGroup(hotelGroup)
        .build();
  }

}
