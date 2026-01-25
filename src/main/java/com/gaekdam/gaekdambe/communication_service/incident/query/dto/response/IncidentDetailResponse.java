package com.gaekdam.gaekdambe.communication_service.incident.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IncidentDetailResponse {

    private Long incidentCode;

    private Long propertyCode;
    private Long employeeCode;

    private String incidentTitle;
    private String incidentSummary;
    private String incidentContent;

    private String severity;
    private String incidentType;
    private String incidentStatus;

    private LocalDateTime occurredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long inquiryCode;

    private String employeeLoginId;
    private String employeeName;
}
