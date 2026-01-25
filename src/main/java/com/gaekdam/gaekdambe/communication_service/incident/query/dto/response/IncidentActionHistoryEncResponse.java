package com.gaekdam.gaekdambe.communication_service.incident.query.dto.response;

import java.time.LocalDateTime;

public record IncidentActionHistoryEncResponse(
        Long incidentActionHistoryCode,
        Long employeeCode,
        String employeeLoginId,
        byte[] employeeNameEnc,
        byte[] employeeDekEnc,
        String actionContent,
        LocalDateTime createdAt
) {}
