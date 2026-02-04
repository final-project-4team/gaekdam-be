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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="사건/사고")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/incidents")
public class IncidentQueryController {

    private final IncidentQueryService incidentQueryService;

    //사건사고 리스트
    @GetMapping
    @PreAuthorize("hasAuthority('INCIDENT_LIST')")
    @AuditLog(details = "", type = PermissionTypeKey.INCIDENT_LIST)
    @Operation(summary = "사건/사고 조회", description = "호텔에 등록된 사건사고 리스트를 조회한다.")
    public ApiResponse<PageResponse<IncidentListResponse>> getIncidents(
            @AuthenticationPrincipal CustomUser user,
            @Parameter(description = "페이징 값")PageRequest page,
            @Parameter(description = "검색어 키워드")IncidentListSearchRequest search,
            @Parameter(description = "정렬 기준")SortRequest sort
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
    @Operation(summary = "사건/사고 상세 조회", description = "특정 사건사고에 대한 상세 정보를 조회한다.")
    public ApiResponse<IncidentDetailResponse> getIncidentDetail(
            @AuthenticationPrincipal CustomUser user,
            @Parameter(description = "사건/사고 코드")@PathVariable Long incidentCode
    ) {
        return ApiResponse.success(
                incidentQueryService.getIncidentDetail(user.getHotelGroupCode(), incidentCode)
        );
    }

    // 조치 이력 조회
    @GetMapping("/{incidentCode}/actions")
    //@PreAuthorize("hasAuthority('INCIDENT_ACTION_READ')")
    @PreAuthorize("hasAuthority('INCIDENT_READ')")
    @AuditLog(details = "", type = PermissionTypeKey.INCIDENT_ACTION_READ)
    @Operation(summary = "조치 이력 리스트 조회", description = "특정 사건사고에 대한 조치이력 리스트를 조회한다.")
    public ApiResponse<List<IncidentActionHistoryResponse>> getIncidentActions(
            @AuthenticationPrincipal CustomUser user,
            @Parameter(description="사건/사고 코드")@PathVariable Long incidentCode
    ) {
        return ApiResponse.success(
                incidentQueryService.getIncidentActionHistories(user.getHotelGroupCode(), incidentCode)
        );
    }
}
