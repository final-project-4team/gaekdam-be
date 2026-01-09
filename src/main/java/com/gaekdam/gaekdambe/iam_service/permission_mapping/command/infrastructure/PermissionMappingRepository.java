package com.gaekdam.gaekdambe.iam_service.permission_mapping.command.infrastructure;

import com.gaekdam.gaekdambe.iam_service.permission_mapping.command.domain.entity.PermissionMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionMappingRepository extends JpaRepository<PermissionMapping,Long> {

}
