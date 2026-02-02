package com.gaekdam.gaekdambe.iam_service.employee.query.service;

import com.gaekdam.gaekdambe.global.crypto.DecryptionService;
import com.gaekdam.gaekdambe.global.crypto.MaskingUtils;
import com.gaekdam.gaekdambe.global.crypto.SearchHashService;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.request.EmployeeQuerySearchRequest;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeDetailResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeListResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeQueryEncResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeQueryListEncResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.mapper.EmployeeQueryMapper;
import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.AuditLog;
import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.LogPersonalInfo;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeQueryService {

  private final EmployeeQueryMapper employeeQueryMapper;
  private final DecryptionService decryptionService;
  private final SearchHashService searchHashService;

  @LogPersonalInfo(type = PermissionTypeKey.EMPLOYEE_READ, purpose = "직원 인사 정보 조회")
  public EmployeeDetailResponse getEmployeeDetail(Long hotelGroupCode, Long employeeCode, String reason) {

    EmployeeQueryEncResponse response = employeeQueryMapper.findByEmployeeCode(employeeCode);
    if (response == null || !response.hotelGroupCode().equals(hotelGroupCode)) {
      throw new IllegalArgumentException(
          "Not found: " + employeeCode + "or 검색하려는 직원의 hotelGroupCode가 본인의 호텔코드와 일치하지 않습니다.");
    }
    return toDetailDto(response);
  }

  @AuditLog(details = "", type = PermissionTypeKey.EMPLOYEE_LIST)
  public PageResponse<EmployeeListResponse> searchEmployees(Long hotelGroupCode,
      EmployeeQuerySearchRequest request, PageRequest page, SortRequest sort) {

    if (page.getSize() >50) {
      page.setSize(50);
    }

    byte[] nameHash = (request.name() != null) ? searchHashService.nameHash(request.name()) : null;
    byte[] phoneHash = (request.phone() != null) ? searchHashService.phoneHash(request.phone()) : null;
    byte[] emailHash = (request.email() != null) ? searchHashService.emailHash(request.email()) : null;

    long totalElements = employeeQueryMapper.countSearchEmployees(hotelGroupCode, nameHash, phoneHash, emailHash,
        request);
    List<EmployeeQueryListEncResponse> employees = employeeQueryMapper.searchEmployees(
        hotelGroupCode,
        nameHash, phoneHash, emailHash, request, page, sort);

    List<EmployeeListResponse> content = employees.stream()
        .map(this::toListDto)
        .collect(Collectors.toList());

    return new PageResponse<>(content, page.getPage(), page.getSize(), totalElements);
  }

  // 목록용 DTO 변환 (마스킹 적용)
  private EmployeeListResponse toListDto(EmployeeQueryListEncResponse response) {
    Long code = response.employeeCode();
    byte[] dekEnc = response.dekEnc();

    String name = decryptionService.decrypt(code, dekEnc, response.employeeNameEnc());
    String phone = decryptionService.decrypt(code, dekEnc, response.phoneNumberEnc());
    String email = decryptionService.decrypt(code, dekEnc, response.emailEnc());

    return new EmployeeListResponse(
        code,
        response.employeeNumber(),
        response.permissionName(),
        MaskingUtils.maskName(name),
        MaskingUtils.maskPhone(phone),
        (email != null) ? MaskingUtils.maskEmail(email) : null,
        response.loginId(),
        response.employeeStatus());
  }

  // 상세용 DTO 변환 (전체 복호화)
  private EmployeeDetailResponse toDetailDto(EmployeeQueryEncResponse response) {
    Long code = response.employeeCode();
    byte[] dekEnc = response.dekEnc();

    return new EmployeeDetailResponse(
        code,
        response.employeeNumber(),
        response.loginId(),
        decryptionService.decrypt(code, dekEnc, response.employeeNameEnc()),
        decryptionService.decrypt(code, dekEnc, response.phoneNumberEnc()),
        decryptionService.decrypt(code, dekEnc, response.emailEnc()),
        response.departmentName(),
        response.hotelPositionName(),
        response.propertyName(),
        response.hotelGroupName(),
        response.permissionName(),
        response.departmentCode(),
        response.hotelPositionCode(),
        response.propertyCode(),
        response.hotelGroupCode(),
        response.permissionCode(),
        response.hiredAt(),
        response.employeeStatus(),
        response.createdAt(),
        response.updatedAt(),
        response.failedLoginCount(),
        response.lastLoginAt());
  }

  public EmployeeDetailResponse getMyPage(Long hotelGroupCode, String loginId) {
    EmployeeQueryEncResponse response = employeeQueryMapper.findMyPage(hotelGroupCode, loginId);
    if (response == null || !response.hotelGroupCode().equals(hotelGroupCode)) {
      throw new IllegalArgumentException("Not found: " + loginId + "or Not match hotelGroupCode");
    }
    return toDetailDto(response);
  }
}
