package com.gaekdam.gaekdambe.communication_service.incident.query.controller;

import com.gaekdam.gaekdambe.communication_service.incident.query.dto.request.IncidentListSearchRequest;
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
        // SaaS 스코프 강제 (다른 호텔그룹 데이터 차단)
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
}
