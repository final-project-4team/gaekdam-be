package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportLayout {
    private Long layoutId;
    private Long employeeCode;
    private Long tenantCode;
    private String name;
    private String description;
    private Boolean isDefault;
    private Boolean isArchived;
    private String visibilityScope; // Consider replacing with enum later
    private String dateRangePreset;
    private String defaultFilterJson;
    private String layoutJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}