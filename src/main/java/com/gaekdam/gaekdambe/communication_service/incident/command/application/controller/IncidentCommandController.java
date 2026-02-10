package com.gaekdam.gaekdambe.communication_service.incident.command.application.controller;

import com.gaekdam.gaekdambe.communication_service.incident.command.application.dto.request.IncidentCreateRequest;
import com.gaekdam.gaekdambe.communication_service.incident.command.application.dto.response.IncidentCreateResponse;
import com.gaekdam.gaekdambe.communication_service.incident.command.application.service.IncidentCommandService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사건/사고")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/incidents")
public class IncidentCommandController {

    private final IncidentCommandService incidentCommandService;

    //사건사고 등록
    @PostMapping
    @PreAuthorize("hasAuthority('INCIDENT_CREATE')")
    @Operation(summary = "사건/사고 등록", description = "사건/사고 정보를 등록한다.")
    public ApiResponse<IncidentCreateResponse> createIncident(@Valid @RequestBody IncidentCreateRequest request) {
        Long incidentCode = incidentCommandService.createIncident(request);
        return ApiResponse.success(new IncidentCreateResponse(incidentCode));
    }

    @PatchMapping("/{incidentCode}/close")
    @PreAuthorize("hasAuthority('INCIDENT_CREATE')")
    @Operation(summary = "사건/사고 처리 완료", description = "사건/사고를 처리 완료된 상태로 변경한다.")
    public ApiResponse<Void> closeIncident(
        @Parameter(description = "사고 코드")@PathVariable Long incidentCode)
    {
        incidentCommandService.closeIncident(incidentCode);
        return ApiResponse.success(null);
    }

}
