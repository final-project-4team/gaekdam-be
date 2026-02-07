package com.gaekdam.gaekdambe.iam_service.permission.command.application.service;

import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.AuditLog;
import com.gaekdam.gaekdambe.iam_service.permission.command.application.dto.request.PermissionCreateRequest;
import com.gaekdam.gaekdambe.iam_service.permission.command.application.dto.request.PermissionUpdateRequest;
import com.gaekdam.gaekdambe.iam_service.permission.command.domain.entity.Permission;
import com.gaekdam.gaekdambe.iam_service.permission.command.infrastructure.PermissionRepository;
import com.gaekdam.gaekdambe.iam_service.permission_mapping.command.domain.entity.PermissionMapping;
import com.gaekdam.gaekdambe.iam_service.permission_mapping.command.infrastructure.PermissionMappingRepository;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.entity.PermissionType;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.infrastructure.PermissionTypeRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermissionCommandService {

  private final HotelGroupRepository hotelGroupRepository;
  private final PermissionTypeRepository permissionTypeRepository;
  private final PermissionMappingRepository permissionMappingRepository;
  private final PermissionRepository permissionRepository;
  private final com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository employeeRepository;
  private final com.gaekdam.gaekdambe.iam_service.log.command.application.service.AuditLogService auditLogService;

  // 권한 생성

  @Transactional
  @AuditLog(details = "'권한 이름 : '+ #request.permissionName", type = PermissionTypeKey.PERMISSION_CREATE)
  public String createPermission(PermissionCreateRequest request, Long hotelGroupCode) {
    HotelGroup hotelGroup = hotelGroupRepository.findById(hotelGroupCode).orElseThrow();

    if(permissionRepository.existsByHotelGroup_HotelGroupCodeAndPermissionName(hotelGroup.getHotelGroupCode(),request.permissionName())){
      throw new CustomException(ErrorCode.PERMISSION_NAME_DUPLICATE);
    }
    // 권한이름 과 호텔 참조키로 권한 생성
    Permission permission = permissionRepository.save(
        Permission.createPermission(request.permissionName(), hotelGroup));

    List<PermissionType> permissionTypes = permissionTypeRepository.findAllById(
        request.permissionTypeList());

    List<PermissionMapping> mappings = permissionTypes.stream()
        .map(pt -> PermissionMapping.createPermissionMapping(permission, pt))
        .collect(Collectors.toList());
    permissionMappingRepository.saveAll(mappings);

    return "권한 생성 완료";
  }


  //  수동 로깅 (Diff)
  @Transactional
  public String updatePermission(Long permissionCode, PermissionUpdateRequest request,
      Long hotelGroupCode, String accessorLoginId) {
    Permission permission = permissionRepository.findById(permissionCode).orElseThrow();
    if (!permission.getHotelGroup().getHotelGroupCode().equals(hotelGroupCode)) {
      throw new CustomException(ErrorCode.HOTEL_GROUP_CODE_NOT_MATCH);
    }

    // Accessor 조회
    com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee accessor = employeeRepository
        .findByLoginId(accessorLoginId).orElseThrow(() -> new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND));

    // --- 변경 전 데이터 캡처 ---
    String prevName = permission.getPermissionName();
    List<PermissionMapping> prevMappings = permissionMappingRepository.findAllByPermission(permission);
    String prevTypes = prevMappings.stream()
        .map(pm -> pm.getPermissionType().getPermissionTypeKey().name())
        .sorted()
        .collect(Collectors.joining(", "));

    permissionMappingRepository.deleteAllByPermission(permission);

    // 권한 타입리스트 검색
    List<PermissionType> permissionTypes = permissionTypeRepository.findAllById(
        request.permissionTypeList());

    // 권한 타입 매핑에 권한과 권한 타입 참조키로 생성
    List<PermissionMapping> mappings = permissionTypes.stream()
        .map(pt -> PermissionMapping.createPermissionMapping(permission, pt))
        .collect(Collectors.toList());

    // 권한 매핑 리스트 저장
    permission.setUpdatedAt();
    permissionRepository.save(permission);
    permissionMappingRepository.saveAll(mappings);

    // --- 변경 후 데이터 캡처 & 비교 ---
    String newName = permission.getPermissionName();

    String newTypes = permissionTypes.stream()
        .map(pt -> pt.getPermissionTypeKey().name())
        .sorted()
        .collect(Collectors.joining(", "));

    StringBuilder changes = new StringBuilder();
    StringBuilder prevVal = new StringBuilder();
    StringBuilder newVal = new StringBuilder();

    if (!prevTypes.equals(newTypes)) {
      changes.append("[권한 타입 변경] ");
      prevVal.append(String.format("Types: %s", prevTypes));
      newVal.append(String.format("Types: %s", newTypes));
    }

    String details = changes.length() > 0 ? changes.toString() : "권한 수정 (변동 사항 없음)";

    auditLogService.saveAuditLog(
        accessor,
        PermissionTypeKey.PERMISSION_UPDATE,
        details,
        (prevVal.length() > 0 ? prevVal.toString() : null),
        (newVal.length() > 0 ? newVal.toString() : null));

    return "권한 수정 완료";
  }

  // 권한 삭제
  @Transactional
  @AuditLog(details = "'권한 코드 : '+ #permissionCode", type = PermissionTypeKey.PERMISSION_DELETE)
  public String deletePermission(Long permissionCode, Long hotelGroupCode) {
    Permission permission = permissionRepository.findById(permissionCode).orElseThrow();
    if (!permission.getHotelGroup().getHotelGroupCode().equals(hotelGroupCode)) {
      throw new CustomException(ErrorCode.HOTEL_GROUP_CODE_NOT_MATCH);
    }

    // 1번 권한(기본 권한) 존재 여부 확인
    boolean defaultPermissionExists = permissionRepository.existsById(1L);
    if (!defaultPermissionExists) {
      throw new CustomException(ErrorCode.PERMISSION_NOT_FOUND, "기본 권한(ID:1)이 존재하지 않아 이동할 수 없습니다.");
    }

    // 해당 권한을 가진 직원들을 1번 권한으로 일괄 이동
    employeeRepository.bulkUpdatePermission(permissionCode, 1L);

    permissionMappingRepository.deleteAllByPermission(permission);
    permission.deletePermission();
    permissionRepository.save(permission);
    return "권한 삭제";
  }
}
