package com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="hotel_group")
public class HotelGroup {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="hotel_group_code")
  private Long hotelGroupCode;

  @Column(name="hotel_group_name",nullable = false)
  private String hotelGroupName;

  @Column(name="hotel_expired_at",nullable = false)
  private LocalDateTime hotelExpiredAt;


  public static HotelGroup createHotelGroup(
      String hotelGroupName,
      LocalDateTime hotelExpiredAt
  )
  {
   return HotelGroup.builder()
       .hotelGroupName(hotelGroupName)
       .hotelExpiredAt(hotelExpiredAt)
       .build();
  }
}
