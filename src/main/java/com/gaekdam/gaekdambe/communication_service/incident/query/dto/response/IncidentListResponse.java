package com.gaekdam.gaekdambe.communication_service.incident.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IncidentListResponse {

    private Long incidentCode;
    private LocalDateTime createdAt;

    private String incidentTitle;
    private String incidentStatus;
    private String severity;
    private String incidentType;

    private Long propertyCode;
    private Long employeeCode;

    private Long inquiryCode;

    private String employeeLoginId;
    private String employeeName;
}
