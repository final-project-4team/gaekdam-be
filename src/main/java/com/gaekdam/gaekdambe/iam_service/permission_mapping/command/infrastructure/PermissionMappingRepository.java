package com.gaekdam.gaekdambe.iam_service.permission_mapping.command.infrastructure;

import com.gaekdam.gaekdambe.iam_service.permission.command.domain.entity.Permission;
import com.gaekdam.gaekdambe.iam_service.permission_mapping.command.domain.entity.PermissionMapping;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.entity.PermissionType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionMappingRepository extends JpaRepository<PermissionMapping, Long> {
    List<PermissionMapping> findAllByPermission(Permission permission);

  Optional<PermissionMapping> findByPermissionTypeAndPermission(PermissionType permissionType, Permission permission);

  void deleteAllByPermission(Permission permission);
}
