package com.gaekdam.gaekdambe.iam_service.employee.query.service;

import com.gaekdam.gaekdambe.global.crypto.DecryptionService;
import com.gaekdam.gaekdambe.global.crypto.MaskingUtils;
import com.gaekdam.gaekdambe.global.crypto.SearchHashService;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeDetailResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeListResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeQueryEncResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.mapper.EmployeeQueryMapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeQueryService {

    private final EmployeeQueryMapper employeeQueryMapper;
    private final DecryptionService decryptionService;
    private final SearchHashService searchHashService;

    public List<EmployeeListResponse> getEmployeeList() {
        List<EmployeeQueryEncResponse> employees = employeeQueryMapper.findAllEmployees();
        return employees.stream().map(this::toListDto).collect(Collectors.toList());
    }

    public EmployeeDetailResponse getEmployeeDetail(Long employeeCode) {
        EmployeeQueryEncResponse response = employeeQueryMapper.findByEmployeeCode(employeeCode);
        if (response == null)
            throw new IllegalArgumentException("Not found: " + employeeCode);
        return toDetailDto(response);
    }

    public PageResponse<EmployeeListResponse> searchEmployees(String name, String phone, String email,
            Pageable pageable) {
        byte[] nameHash = (name != null) ? searchHashService.nameHash(name) : null;
        byte[] phoneHash = (phone != null) ? searchHashService.phoneHash(phone) : null;
        byte[] emailHash = (email != null) ? searchHashService.emailHash(email) : null;

        long totalElements = employeeQueryMapper.countSearchEmployees(nameHash, phoneHash, emailHash);
        List<EmployeeQueryEncResponse> employees = employeeQueryMapper.searchEmployees(
                nameHash, phoneHash, emailHash, pageable.getOffset(), pageable.getPageSize());

        List<EmployeeListResponse> content = employees.stream()
                .map(this::toListDto)
                .collect(Collectors.toList());

        return new PageResponse<>(content, pageable.getPageNumber(), pageable.getPageSize(), totalElements);
    }

    public List<EmployeeListResponse> findByPassword(String password) {
        List<EmployeeQueryEncResponse> employees = employeeQueryMapper.findByPassword(password);
        return employees.stream().map(this::toListDto).collect(Collectors.toList());
    }

    // 목록용 DTO 변환 (마스킹 적용)
    private EmployeeListResponse toListDto(EmployeeQueryEncResponse response) {
        Long code = response.employeeCode();
        byte[] dekEnc = response.dekEnc();

        String name = decryptionService.decrypt(code, dekEnc, response.employeeNameEnc());
        String phone = decryptionService.decrypt(code, dekEnc, response.phoneNumberEnc());
        String email = decryptionService.decrypt(code, dekEnc, response.emailEnc());

        return new EmployeeListResponse(
                code,
                response.employeeNumber(),
                response.loginId(),
                MaskingUtils.maskName(name),
                MaskingUtils.maskPhone(phone),
                (email != null) ? MaskingUtils.maskEmail(email) : null,
                response.departmentName(),
                response.hotelPositionName(),
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
                response.hiredAt(),
                response.employeeStatus(),
                response.createdAt(),
                response.updatedAt(),
                response.failedLoginCount(),
                response.lastLoginAt());
    }
}
