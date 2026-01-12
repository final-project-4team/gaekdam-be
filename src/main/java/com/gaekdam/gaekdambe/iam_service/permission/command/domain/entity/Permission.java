package com.gaekdam.gaekdambe.iam_service.permission.command.domain.entity;

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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name="permission")
public class Permission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="permission_code")
  private Long permissionCode;

  @Column(name="permission_name",nullable = false,unique = true)
  private String permissionName;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name="hotel_group_code",nullable = false)
  private HotelGroup hotelGroup;

  private Permission(String permissionName, HotelGroup hotelGroup) {
    if (hotelGroup == null) throw new IllegalArgumentException("hotelGroupCode is required");
    if (permissionName == null || permissionName.isBlank()) throw new IllegalArgumentException("permissionName is required");

    this.permissionName = permissionName;
    this.hotelGroup =hotelGroup;
  }

  public static Permission createPermission(String permissionName, HotelGroup hotelGroup ){
    return new Permission(permissionName, hotelGroup);
  }

}
