package com.gaekdam.gaekdambe.communication_service.incident.query.service;

import com.gaekdam.gaekdambe.communication_service.incident.query.dto.request.IncidentListSearchRequest;
import com.gaekdam.gaekdambe.communication_service.incident.query.dto.response.*;
import com.gaekdam.gaekdambe.communication_service.incident.query.mapper.IncidentMapper;
import com.gaekdam.gaekdambe.global.crypto.DecryptionService;
import com.gaekdam.gaekdambe.global.crypto.MaskingUtils;
import com.gaekdam.gaekdambe.global.crypto.SearchHashService;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.LogPersonalInfo;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IncidentQueryService {

    private final IncidentMapper incidentMapper;
    private final DecryptionService decryptionService;
    private final SearchHashService searchHashService;

    public PageResponse<IncidentListResponse> getIncidents(PageRequest page,
                                                           IncidentListSearchRequest search,
                                                           SortRequest sort) {

        // ✅ 핵심: EMPLOYEE_NAME / ALL이면 employeeNameHash 세팅
        String keyword = trim(search.getKeyword());
        String searchType = trim(search.getSearchType());
        if (searchType == null) searchType = "ALL";

        if (keyword != null && !keyword.isBlank()) {
            if ("EMPLOYEE_NAME".equals(searchType) || "ALL".equals(searchType)) {
                search.setEmployeeNameHash(searchHashService.nameHash(keyword));
            } else {
                // 다른 타입일 때는 hash 비교 안 함
                search.setEmployeeNameHash(null);
            }
        } else {
            search.setEmployeeNameHash(null);
        }

        List<IncidentListEncResponse> rows = incidentMapper.findIncidents(page, search, sort);
        long total = incidentMapper.countIncidents(search);

        List<IncidentListResponse> content = rows.stream()
                .map(this::toListDto)
                .toList();

        return new PageResponse<>(content, page.getPage(), page.getSize(), total);
    }

    @LogPersonalInfo(type = PermissionTypeKey.CUSTOMER_READ, purpose = "고객 정보 조회")
    public IncidentDetailResponse getIncidentDetail(Long hotelGroupCode, Long incidentCode) {
        IncidentDetailEncResponse row = incidentMapper.findIncidentDetail(hotelGroupCode, incidentCode);
        if (row == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "존재하지 않는 사건/사고입니다.");
        }
        return toDetailDto(row);
    }

    public List<IncidentActionHistoryResponse> getIncidentActionHistories(Long hotelGroupCode, Long incidentCode) {
        List<IncidentActionHistoryEncResponse> rows =
                incidentMapper.findIncidentActionHistories(hotelGroupCode, incidentCode);

        return rows.stream().map(r -> new IncidentActionHistoryResponse(
                r.incidentActionHistoryCode(),
                r.employeeCode(),
                r.employeeLoginId(),
                decryptEmployeeName(r.employeeCode(), r.employeeDekEnc(), r.employeeNameEnc()),
                r.actionContent(),
                r.createdAt()
        )).toList();
    }

    private IncidentListResponse toListDto(IncidentListEncResponse r) {
        String employeeName = decryptEmployeeName(r.employeeCode(), r.employeeDekEnc(), r.employeeNameEnc());
        employeeName = MaskingUtils.maskName(employeeName);

        return new IncidentListResponse(
                r.incidentCode(),
                r.createdAt(),
                r.incidentTitle(),
                r.incidentStatus(),
                r.severity(),
                r.incidentType(),
                r.propertyCode(),
                r.employeeCode(),
                r.inquiryCode(),
                r.employeeLoginId(),
                employeeName
        );
    }

    private IncidentDetailResponse toDetailDto(IncidentDetailEncResponse r) {
        String employeeName = decryptEmployeeName(r.employeeCode(), r.employeeDekEnc(), r.employeeNameEnc());

        return new IncidentDetailResponse(
                r.incidentCode(),
                r.propertyCode(),
                r.employeeCode(),
                r.incidentTitle(),
                r.incidentSummary(),
                r.incidentContent(),
                r.severity(),
                r.incidentType(),
                r.incidentStatus(),
                r.occurredAt(),
                r.createdAt(),
                r.updatedAt(),
                r.inquiryCode(),
                r.employeeLoginId(),
                employeeName
        );
    }

    private String decryptEmployeeName(Long employeeCode, byte[] dekEnc, byte[] nameEnc) {
        if (employeeCode == null || dekEnc == null || nameEnc == null) return null;
        return decryptionService.decrypt(employeeCode, dekEnc, nameEnc);
    }

    private String trim(String v) {
        return (v == null) ? null : v.trim();
    }
}
