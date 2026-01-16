package com.gaekdam.gaekdambe.communication_service.incident.command.application.controller;

import com.gaekdam.gaekdambe.communication_service.incident.command.application.dto.request.IncidentCreateRequest;
import com.gaekdam.gaekdambe.communication_service.incident.command.application.dto.response.IncidentCreateResponse;
import com.gaekdam.gaekdambe.communication_service.incident.command.application.service.IncidentCommandService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/incidents")
public class IncidentCommandController {

    private final IncidentCommandService incidentCommandService;

    @PostMapping
    public ApiResponse<IncidentCreateResponse> createIncident(@Valid @RequestBody IncidentCreateRequest request) {
        Long incidentCode = incidentCommandService.createIncident(request);
        return ApiResponse.success(new IncidentCreateResponse(incidentCode));
    }
}
