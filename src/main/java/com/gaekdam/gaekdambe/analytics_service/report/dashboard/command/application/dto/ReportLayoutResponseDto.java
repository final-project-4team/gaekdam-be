package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportLayoutResponseDto {
    private Long layoutId;
    private Long employeeCode;
    private String name;
    private String description;
    private Boolean isDefault;
    private Boolean isArchived;
    private String visibilityScope;
    private String dateRangePreset;
    private String defaultFilterJson;
    private String layoutJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}