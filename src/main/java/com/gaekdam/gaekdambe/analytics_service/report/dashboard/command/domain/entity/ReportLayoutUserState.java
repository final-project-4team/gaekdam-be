package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportLayoutUserState {
    private Long layoutId;
    private Long employeeCode;
    private String stateJson;
    private LocalDateTime updatedAt;
}
