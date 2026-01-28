package com.gaekdam.gaekdambe.iam_service.permission_mapping.command.infrastructure;

import com.gaekdam.gaekdambe.iam_service.permission.command.domain.entity.Permission;
import com.gaekdam.gaekdambe.iam_service.permission_mapping.command.domain.entity.PermissionMapping;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.entity.PermissionType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PermissionMappingRepository extends JpaRepository<PermissionMapping, Long> {
  List<PermissionMapping> findAllByPermission(Permission permission);

  @Query("SELECT pm FROM PermissionMapping pm JOIN FETCH pm.permissionType WHERE pm.permission = :permission")
  List<PermissionMapping> findAllByPermissionWithPermissionType(@Param("permission") Permission permission);

  Optional<PermissionMapping> findByPermissionTypeAndPermission(PermissionType permissionType, Permission permission);

  void deleteAllByPermission(Permission permission);
}
