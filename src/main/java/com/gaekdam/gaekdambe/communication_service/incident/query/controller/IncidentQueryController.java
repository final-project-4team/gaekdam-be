package com.gaekdam.gaekdambe.communication_service.incident.query.controller;

import com.gaekdam.gaekdambe.communication_service.incident.query.dto.request.IncidentListSearchRequest;
import com.gaekdam.gaekdambe.communication_service.incident.query.dto.response.IncidentActionHistoryResponse;
import com.gaekdam.gaekdambe.communication_service.incident.query.dto.response.IncidentDetailResponse;
import com.gaekdam.gaekdambe.communication_service.incident.query.dto.response.IncidentListResponse;
import com.gaekdam.gaekdambe.communication_service.incident.query.service.IncidentQueryService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/incidents")
public class IncidentQueryController {

    private final IncidentQueryService incidentQueryService;

    @GetMapping
    public ApiResponse<PageResponse<IncidentListResponse>> getIncidents(
            @AuthenticationPrincipal CustomUser user,
            PageRequest page,
            IncidentListSearchRequest search,
            SortRequest sort
    ) {
        search.setHotelGroupCode(user.getHotelGroupCode());

        if (sort == null || sort.getSortBy() == null) {
            sort = new SortRequest();
            sort.setSortBy("created_at");
            sort.setDirection("DESC");
        }

        return ApiResponse.success(
                incidentQueryService.getIncidents(page, search, sort)
        );
    }

    @GetMapping("/{incidentCode}")
    public ApiResponse<IncidentDetailResponse> getIncidentDetail(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long incidentCode
    ) {
        return ApiResponse.success(
                incidentQueryService.getIncidentDetail(user.getHotelGroupCode(), incidentCode)
        );
    }

    // 조치 이력 조회
    @GetMapping("/{incidentCode}/actions")
    public ApiResponse<List<IncidentActionHistoryResponse>> getIncidentActions(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long incidentCode
    ) {
        return ApiResponse.success(
                incidentQueryService.getIncidentActionHistories(user.getHotelGroupCode(), incidentCode)
        );
    }
}
