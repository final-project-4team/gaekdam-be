package com.gaekdam.gaekdambe.communication_service.incident.command.application.dto.request;

import com.gaekdam.gaekdambe.communication_service.incident.command.domain.IncidentSeverity;
import com.gaekdam.gaekdambe.communication_service.incident.command.domain.IncidentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class IncidentCreateRequest {

    @NotNull
    private Long propertyCode;

    @NotNull
    private Long employeeCode; // 책임자

    private Long inquiryCode; // 선택

    @NotBlank
    private String incidentTitle;

    private String incidentSummary;

    @NotBlank
    private String incidentContent;

    @NotNull
    private IncidentType incidentType;

    private IncidentSeverity severity; // null이면 MEDIUM 처리

    private LocalDateTime occurredAt;
}
