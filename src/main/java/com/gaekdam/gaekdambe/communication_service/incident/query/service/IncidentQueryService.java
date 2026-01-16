package com.gaekdam.gaekdambe.communication_service.incident.query.service;

import com.gaekdam.gaekdambe.communication_service.incident.query.dto.request.IncidentListSearchRequest;
import com.gaekdam.gaekdambe.communication_service.incident.query.dto.response.IncidentDetailResponse;
import com.gaekdam.gaekdambe.communication_service.incident.query.dto.response.IncidentListResponse;
import com.gaekdam.gaekdambe.communication_service.incident.query.mapper.IncidentMapper;
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

    public PageResponse<IncidentListResponse> getIncidents(
            PageRequest page,
            IncidentListSearchRequest search,
            SortRequest sort
    ) {
        List<IncidentListResponse> list =
                incidentMapper.findIncidents(page, search, sort);

        long total =
                incidentMapper.countIncidents(search);

        return new PageResponse<>(list, page.getPage(), page.getSize(), total);
    }

    public IncidentDetailResponse getIncidentDetail(Long hotelGroupCode, Long incidentCode) {
        IncidentDetailResponse detail = incidentMapper.findIncidentDetail(hotelGroupCode, incidentCode);
        if (detail == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "존재하지 않는 사건사고(incidentCode)입니다.");
        }
        return detail;
    }
}
