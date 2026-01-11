package com.gaekdam.gaekdambe.iam_service.permission_mapping.command.infrastructure;

import com.gaekdam.gaekdambe.iam_service.permission.command.domain.entity.Permission;
import com.gaekdam.gaekdambe.iam_service.permission_mapping.command.domain.entity.PermissionMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionMappingRepository extends JpaRepository<PermissionMapping, Long> {
    List<PermissionMapping> findAllByPermission(Permission permission);
}
