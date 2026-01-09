package com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

  @Column(name="permission_type_key",nullable = false)
  private String permissionTypeKey;//여기서 식별

  @Column(name="permission_type_name",nullable = false)
  private String permissionTypeName;//화면 표시용


  @Column(name="permission_type_resource",nullable = false)
  private String permissionTypeResource;//접근 데이터

  @Column(name="permission_type_action",nullable = false)
  private String permissionTypeAction;//행위 종류

  @Column(name = "hotel_group_code",nullable = false)
  private Long hotelGroupCode;

  private PermissionType(String permissionTypeKey,String permissionTypeName,String permissionTypeResource,String permissionTypeAction, Long hotelGroupCode) {
    if (permissionTypeKey == null || permissionTypeKey.isBlank()) throw new IllegalArgumentException("permissionTypeKey is required");
    if (permissionTypeName == null || permissionTypeName.isBlank()) throw new IllegalArgumentException("permissionTypeKey is required");
    if (permissionTypeResource == null || permissionTypeResource.isBlank()) throw new IllegalArgumentException("permissionTypeKey is required");
    if (permissionTypeAction == null || permissionTypeAction.isBlank()) throw new IllegalArgumentException("permissionTypeKey is required");
    if (hotelGroupCode == null) throw new IllegalArgumentException("hotelGroupCode is required");

    this.permissionTypeKey = permissionTypeKey;
    this.permissionTypeName = permissionTypeName;
    this.permissionTypeResource = permissionTypeResource;
    this.permissionTypeAction = permissionTypeAction;
    this.hotelGroupCode = hotelGroupCode;
  }

  public static PermissionType createPermissionType(String permissionTypeKey,String permissionTypeName,String permissionTypeResource,String permissionTypeAction, Long hotelGroupCode) {
    return new PermissionType(permissionTypeKey,permissionTypeName,permissionTypeResource,permissionTypeAction,hotelGroupCode);
  }
}
