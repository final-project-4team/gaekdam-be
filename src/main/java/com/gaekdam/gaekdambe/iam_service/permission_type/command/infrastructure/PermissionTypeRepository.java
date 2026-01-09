package com.gaekdam.gaekdambe.iam_service.permission_type.command.infrastructure;

import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.entity.PermissionType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionTypeRepository extends JpaRepository<PermissionType, Long> {

  Optional<PermissionType> findByPermissionTypeKey(String permissionTypeKey);
}
