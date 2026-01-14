package com.gaekdam.gaekdambe.iam_service.permission_mapping.command.domain.entity;

import com.gaekdam.gaekdambe.iam_service.permission.command.domain.entity.Permission;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.entity.PermissionType;
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
@Table(name = "permission_mapping")
public class PermissionMapping {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "permission_mapping_code")
  private Long permissionMappingCode;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "permission_code", nullable = false)
  private Permission permission;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "permission_type_code", nullable = false)
  private PermissionType permissionType;

  private PermissionMapping(Permission permission, PermissionType permissionType) {
    this.permission = permission;
    this.permissionType = permissionType;
  }

  public static PermissionMapping createPermissionMapping(Permission permission, PermissionType permissionType) {
    return new PermissionMapping(permission, permissionType);
  }


}
