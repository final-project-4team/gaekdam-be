package com.gaekdam.gaekdambe.communication_service.incident.command.application.controller;

import com.gaekdam.gaekdambe.communication_service.incident.command.application.dto.request.IncidentCreateRequest;
import com.gaekdam.gaekdambe.communication_service.incident.command.application.dto.response.IncidentCreateResponse;
import com.gaekdam.gaekdambe.communication_service.incident.command.application.service.IncidentCommandService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/incidents")
public class IncidentCommandController {

    private final IncidentCommandService incidentCommandService;

    //사건사고 등록
    @PostMapping
    @PreAuthorize("hasAuthority('INCIDENT_CREATE')")
    public ApiResponse<IncidentCreateResponse> createIncident(@Valid @RequestBody IncidentCreateRequest request) {
        Long incidentCode = incidentCommandService.createIncident(request);
        return ApiResponse.success(new IncidentCreateResponse(incidentCode));
    }

    @PatchMapping("/{incidentCode}/close")
    public ApiResponse<Void> closeIncident(@PathVariable Long incidentCode) {
        incidentCommandService.closeIncident(incidentCode);
        return ApiResponse.success(null);
    }

}
