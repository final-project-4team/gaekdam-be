package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportLayoutUpdateDto {
    private Long layoutId;
    private String name;
    private String description;
    private Boolean isDefault;
    private Boolean isArchived;
    private String visibilityScope;
    private String dateRangePreset;
    private String defaultFilterJson;
    private String layoutJson;
}