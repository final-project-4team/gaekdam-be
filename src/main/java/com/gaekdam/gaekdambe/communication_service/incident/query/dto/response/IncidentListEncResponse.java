package com.gaekdam.gaekdambe.communication_service.incident.query.dto.response;

import java.time.LocalDateTime;

public record IncidentListEncResponse(
        Long incidentCode,
        LocalDateTime createdAt,
        String incidentTitle,
        String incidentStatus,
        String severity,
        String incidentType,
        Long propertyCode,
        Long employeeCode,
        Long inquiryCode,
        String employeeLoginId,
        byte[] employeeNameEnc,
        byte[] employeeDekEnc
) {}
