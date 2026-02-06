package com.gaekdam.gaekdambe.communication_service.incident.query.dto.response;

import java.time.LocalDateTime;

public record IncidentDetailEncResponse(
        Long incidentCode,
        Long propertyCode,
        Long employeeCode,
        String incidentTitle,
        String incidentSummary,
        String incidentContent,
        String severity,
        String incidentType,
        String incidentStatus,
        LocalDateTime occurredAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long inquiryCode,
        String employeeLoginId,
        byte[] employeeNameEnc,
        byte[] employeeDekEnc
) {}
