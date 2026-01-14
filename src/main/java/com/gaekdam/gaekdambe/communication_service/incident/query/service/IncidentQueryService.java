package com.gaekdam.gaekdambe.communication_service.incident.query.service;

import com.gaekdam.gaekdambe.communication_service.incident.query.dto.request.IncidentListSearchRequest;
import com.gaekdam.gaekdambe.communication_service.incident.query.dto.response.IncidentDetailResponse;
import com.gaekdam.gaekdambe.communication_service.incident.query.dto.response.IncidentListResponse;
import com.gaekdam.gaekdambe.communication_service.incident.query.mapper.IncidentMapper;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
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
        return incidentMapper.findIncidentDetail(hotelGroupCode, incidentCode);
    }
}
