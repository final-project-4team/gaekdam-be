package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReportLayoutUserStateResponse {
    private Long layoutId;
    private Long employeeCode;
    private String stateJson;
    private LocalDateTime updatedAt;
}
