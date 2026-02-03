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
import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.AuditLog;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/incidents")
public class IncidentQueryController {

    private final IncidentQueryService incidentQueryService;

    //사건사고 리스트
    @GetMapping
    @PreAuthorize("hasAuthority('INCIDENT_LIST')")
    @AuditLog(details = "", type = PermissionTypeKey.INCIDENT_LIST)
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

    //사건사고 상세조회
    @GetMapping("/{incidentCode}")
    @PreAuthorize("hasAuthority('INCIDENT_READ')")
    @AuditLog(details = "", type = PermissionTypeKey.INCIDENT_READ)
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
    @PreAuthorize("hasAuthority('INCIDENT_ACTION_READ')")
    @AuditLog(details = "", type = PermissionTypeKey.INCIDENT_ACTION_READ)
    public ApiResponse<List<IncidentActionHistoryResponse>> getIncidentActions(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long incidentCode
    ) {
        return ApiResponse.success(
                incidentQueryService.getIncidentActionHistories(user.getHotelGroupCode(), incidentCode)
        );
    }
}
