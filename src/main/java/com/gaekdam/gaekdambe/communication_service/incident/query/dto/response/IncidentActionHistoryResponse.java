package com.gaekdam.gaekdambe.communication_service.incident.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IncidentActionHistoryResponse {

    private Long incidentActionHistoryCode;
    private Long employeeCode;
    private String employeeLoginId;
    private String employeeName; // 복호화
    private String actionContent;
    private LocalDateTime createdAt;
}
