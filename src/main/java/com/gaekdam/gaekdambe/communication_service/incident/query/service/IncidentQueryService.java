package com.gaekdam.gaekdambe.communication_service.incident.query.service;

import com.gaekdam.gaekdambe.communication_service.incident.query.dto.request.IncidentListSearchRequest;
import com.gaekdam.gaekdambe.communication_service.incident.query.dto.response.*;
import com.gaekdam.gaekdambe.communication_service.incident.query.mapper.IncidentMapper;
import com.gaekdam.gaekdambe.global.crypto.DecryptionService;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
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

    public PageResponse<IncidentListResponse> getIncidents(PageRequest page,
                                                           IncidentListSearchRequest search,
                                                           SortRequest sort) {

        List<IncidentListEncResponse> rows = incidentMapper.findIncidents(page, search, sort);
        long total = incidentMapper.countIncidents(search);

        List<IncidentListResponse> content = rows.stream()
                .map(this::toListDto)
                .toList();

        return new PageResponse<>(content, page.getPage(), page.getSize(), total);
    }
    public IncidentDetailResponse getIncidentDetail(Long hotelGroupCode, Long incidentCode) {
        IncidentDetailEncResponse row = incidentMapper.findIncidentDetail(hotelGroupCode, incidentCode);
        if (row == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "존재하지 않는 사건/사고입니다.");
        }
        return toDetailDto(row);
    }

    // 조치 이력 조회

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
}
