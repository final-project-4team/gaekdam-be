package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.request;

import lombok.Data;

@Data
public class ReportLayoutUserStateRequest {
    private Long layoutId;
    private Long employeeCode;
    private String stateJson;
}
