package com.gaekdam.gaekdambe.communication_service.incident.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class IncidentActionCreateRequest {

    @NotBlank
    private String actionContent;
}
