package com.gaekdam.gaekdambe.hotel_service.position.command.domain.entity;

import com.gaekdam.gaekdambe.hotel_service.department.command.domain.entity.Department;
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
@Table(name="hotel_position")
public class HotelPosition {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="hotel_position_code")
  private Long hotelPositionCode;

  @Column(name = "hotel_position_name",nullable = false)
  private String hotelPositionName;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name="department_code",nullable = false)
  private Department department;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name="hotel_group_code",nullable = false)
  private HotelGroup hotelGroup;

  public static HotelPosition createHotelPosition(String hotelPositionName,Department department,HotelGroup hotelGroup) {
    return HotelPosition.builder()
        .hotelPositionName(hotelPositionName)
        .department(department)
        .hotelGroup(hotelGroup)
        .build();
  }
}
