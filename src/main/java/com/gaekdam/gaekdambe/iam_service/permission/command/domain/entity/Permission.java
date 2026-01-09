package com.gaekdam.gaekdambe.iam_service.permission.command.domain.entity;

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
@Table(name="permission")
public class Permission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="permission_code")
  private Long permissionCode;

  @Column(name="permission_name",nullable = false)
  private String permissionName;

/*  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name="hotel_group_code",nullable = false)*/
  @Column(name = "hotel_group_code",nullable = false)
  private Long hotelGroupCode;

  private Permission(String permissionName, Long hotelGroupCode) {
    if (hotelGroupCode == null) throw new IllegalArgumentException("hotelGroupCode is required");
    if (permissionName == null || permissionName.isBlank()) throw new IllegalArgumentException("permissionName is required");

    this.permissionName = permissionName;
    this.hotelGroupCode = hotelGroupCode;
  }

  public static Permission createPermission(String permissionName, Long hotelGroupCode) {
    return new Permission(permissionName, hotelGroupCode);
  }

}
