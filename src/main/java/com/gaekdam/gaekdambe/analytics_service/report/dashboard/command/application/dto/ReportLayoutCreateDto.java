package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportLayoutCreateDto {
    private Long employeeCode;
    private String name;
    private String description;
    private Boolean isDefault;
    private String visibilityScope;
    private String dateRangePreset;
    private String defaultFilterJson;
    private String layoutJson;
}