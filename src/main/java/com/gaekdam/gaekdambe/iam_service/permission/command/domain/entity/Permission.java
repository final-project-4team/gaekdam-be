package com.gaekdam.gaekdambe.iam_service.permission.command.domain.entity;

import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.iam_service.permission.command.domain.PermissionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "permission")
public class Permission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "permission_code")
  private Long permissionCode;

  @Enumerated(EnumType.STRING)
  @Column(name = "permission_status",nullable = false)
  private PermissionStatus permissionStatus;

  @Column(name = "permission_name", nullable = false)
  private String permissionName;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "hotel_group_code", nullable = false)
  private HotelGroup hotelGroup;

  @CreatedDate
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  private Permission(String permissionName, HotelGroup hotelGroup) {
    if (hotelGroup == null)
      throw new IllegalArgumentException("hotelGroupCode is required");
    if (permissionName == null || permissionName.isBlank())
      throw new IllegalArgumentException("permissionName is required");

    this.permissionName = permissionName;
    this.hotelGroup = hotelGroup;
    this.permissionStatus = PermissionStatus.ACTIVE;
  }

  public static Permission createPermission(String permissionName, HotelGroup hotelGroup) {
    return new Permission(permissionName, hotelGroup);
  }

  public void setUpdatedAt(){
    this.updatedAt = LocalDateTime.now();
  }
  public void deletePermission() {
    this.permissionStatus = PermissionStatus.INACTIVE;
  }

}
