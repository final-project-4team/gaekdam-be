package com.gaekdam.gaekdambe.communication_service.incident.command.application.controller;

import com.gaekdam.gaekdambe.communication_service.incident.command.application.dto.request.IncidentActionCreateRequest;
import com.gaekdam.gaekdambe.communication_service.incident.command.application.dto.response.IncidentActionCreateResponse;
import com.gaekdam.gaekdambe.communication_service.incident.command.application.service.IncidentActionCommandService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사건/사고")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/incidents")
public class IncidentActionCommandController {

    private final IncidentActionCommandService incidentActionCommandService;

    // 조치등록
    @PostMapping("/{incidentCode}/actions")
    @PreAuthorize("hasAuthority('INCIDENT_CREATE')")
    @Operation(summary = "조치 등록", description = "특정 사고에 대한 조치 내역을 등록 한다.")
    public ApiResponse<IncidentActionCreateResponse> createAction(
            @AuthenticationPrincipal CustomUser user,
            @Parameter(description = "사건/사고 코드")@PathVariable Long incidentCode,
            @Valid @RequestBody IncidentActionCreateRequest request
    ) {
        Long historyCode = incidentActionCommandService.createAction(
                user.getHotelGroupCode(),
                user.getUsername(),   // loginId
                incidentCode,
                request
        );
        return ApiResponse.success(new IncidentActionCreateResponse(historyCode));
    }
}
