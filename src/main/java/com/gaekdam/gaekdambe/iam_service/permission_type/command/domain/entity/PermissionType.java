package com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.entity;

import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name="permission_type")
public class PermissionType {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="permission_type_code")
  private Long permissionTypeCode;

  @Enumerated(EnumType.STRING)
  @Column(name="permission_type_key",nullable = false)
  private PermissionTypeKey permissionTypeKey;//여기서 식별

  @Column(name="permission_type_name",nullable = false)
  private String permissionTypeName;//화면 표시용


  @Column(name="permission_type_resource",nullable = false)
  private String permissionTypeResource;//접근 데이터

  @Column(name="permission_type_action",nullable = false)
  private String permissionTypeAction;//행위 종류

/*  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name="hotel_group_code",nullable = false)
  private HotelGroup hotelGroup;*/

  private PermissionType(PermissionTypeKey permissionTypeKey,String permissionTypeName,String permissionTypeResource,String permissionTypeAction) {
    if (permissionTypeKey == null) throw new IllegalArgumentException("permissionTypeKey is required");
    if (permissionTypeName == null || permissionTypeName.isBlank()) throw new IllegalArgumentException("permissionTypeKey is required");
    if (permissionTypeResource == null || permissionTypeResource.isBlank()) throw new IllegalArgumentException("permissionTypeKey is required");
    if (permissionTypeAction == null || permissionTypeAction.isBlank()) throw new IllegalArgumentException("permissionTypeKey is required");
 //   if (hotelGroup == null) throw new IllegalArgumentException("hotelGroupCode is required");

    this.permissionTypeKey = permissionTypeKey;
    this.permissionTypeName = permissionTypeName;
    this.permissionTypeResource = permissionTypeResource;
    this.permissionTypeAction = permissionTypeAction;
   // this.hotelGroup=hotelGroup;
  }

  public static PermissionType createPermissionType(PermissionTypeKey permissionTypeKey,String permissionTypeName,String permissionTypeResource,String permissionTypeAction) {
    return new PermissionType(permissionTypeKey,permissionTypeName,permissionTypeResource,permissionTypeAction);
  }
}
