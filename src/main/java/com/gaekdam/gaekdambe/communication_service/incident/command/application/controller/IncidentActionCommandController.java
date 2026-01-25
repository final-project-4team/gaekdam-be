package com.gaekdam.gaekdambe.communication_service.incident.command.application.controller;

import com.gaekdam.gaekdambe.communication_service.incident.command.application.dto.request.IncidentActionCreateRequest;
import com.gaekdam.gaekdambe.communication_service.incident.command.application.dto.response.IncidentActionCreateResponse;
import com.gaekdam.gaekdambe.communication_service.incident.command.application.service.IncidentActionCommandService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/incidents")
public class IncidentActionCommandController {

    private final IncidentActionCommandService incidentActionCommandService;

    @PostMapping("/{incidentCode}/actions")
    public ApiResponse<IncidentActionCreateResponse> createAction(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long incidentCode,
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
