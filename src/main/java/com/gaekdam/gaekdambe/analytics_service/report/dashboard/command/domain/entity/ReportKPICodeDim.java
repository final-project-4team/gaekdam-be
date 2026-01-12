package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportKPICodeDim {
    private String kpiCode;
    private String kpiName;
    private String domainType;
    private String unit;
    private String description;
    private String calcRuleJson;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
