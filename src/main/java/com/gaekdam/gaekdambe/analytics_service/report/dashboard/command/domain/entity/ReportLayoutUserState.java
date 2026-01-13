package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReportLayoutUserState {
    private Long layoutId;
    private Long employeeCode;
    private String stateJson;
    private LocalDateTime updatedAt;
}
