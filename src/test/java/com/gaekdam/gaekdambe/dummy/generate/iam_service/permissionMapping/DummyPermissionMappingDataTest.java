package com.gaekdam.gaekdambe.dummy.generate.iam_service.permissionMapping;

import com.gaekdam.gaekdambe.iam_service.permission.command.domain.entity.Permission;
import com.gaekdam.gaekdambe.iam_service.permission.command.infrastructure.PermissionRepository;
import com.gaekdam.gaekdambe.iam_service.permission_mapping.command.domain.entity.PermissionMapping;
import com.gaekdam.gaekdambe.iam_service.permission_mapping.command.infrastructure.PermissionMappingRepository;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.entity.PermissionType;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.infrastructure.PermissionTypeRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DummyPermissionMappingDataTest {

  @Autowired
  private PermissionRepository permissionRepository;
  @Autowired
  private PermissionMappingRepository permissionMappingRepository;
  @Autowired
  private PermissionTypeRepository permissionTypeRepository;

  @Transactional
  public void generate() {
    if (permissionMappingRepository.count() > 0) {
      return;
    }

    List<Permission> permissions = permissionRepository.findAll();
    List<PermissionType> permissionTypes = permissionTypeRepository.findAll();

    List<PermissionMapping> mappingList = new ArrayList<>();

    for (Permission permission : permissions) {
      String name = permission.getPermissionName();

      for (PermissionType type : permissionTypes) {
        PermissionTypeKey key = type.getPermissionTypeKey();

        if (shouldMap(name, key)) {
          mappingList.add(PermissionMapping.createPermissionMapping(permission, type));
        }
      }
    }
    permissionMappingRepository.saveAll(mappingList);
  }

  private boolean shouldMap(String roleName, PermissionTypeKey key) {
    // 1. 경영 (TOP_MANAGEMENT) - 모든 권한 부여
    if (roleName.contains("총지배인") || roleName.contains("부지배인")) {
      return true;
    }

    // 2. 지원 (회계 등)
    if (roleName.contains("회계")) {
      return key.name().startsWith(" REPORT_LAYOUT_") || key.name().startsWith(" REPORT_LAYOUT_TEMPLATE_")
          || key.name().startsWith(" REPORT_LAYOUT_TEMPLATE_LIBRARY_") || key.name().startsWith("MEMBER_")
          || key.name().startsWith("TODAY_RESERVATION_") || key.name().startsWith("TODAY_FACILITY_USAGE_");
    }

    // 3. 객실 (하우스키핑)
    if (roleName.contains("객실") || roleName.contains("하우스") || roleName.contains("청소")) {
      return key.name().contains("CHECK_IN_") || key.name().contains("CHECK_OUT_")
          || key.name().contains("TODAY_FACILITY_USAGE_")
          || key.name().contains("TODAY_RESERVATION_") || key.name().contains(" INCIDENT_");
    }

    // 4. 식음/조리
    if (roleName.contains("식음") || roleName.contains("조리") || roleName.contains("레스토랑") || roleName.contains("연회")) {
      return key.name().startsWith("CUSTOMER_") || key.name().contains("TODAY_FACILITY_USAGE_")
          || key.name().startsWith("INQUIRY_");
    }

    // 5. 세일즈/홍보
    if (roleName.contains("세일즈") || roleName.contains("홍보") || roleName.contains("마케팅") || roleName.contains("브랜드")) {
      return key.name().startsWith("CUSTOMER_")
          || key.name().startsWith("MESSAGE_") || key.name().startsWith("REPORT_READ");
    }

    // 6. 시설
    if (roleName.contains("시설") || roleName.contains("난방")) {
      return key.name().contains("FACILITY_USAGE") || key.name().startsWith("INCIDENT_")
          || key.name().startsWith("REPORT_READ");
    }

    return false;
  }
}