package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LayoutResponse {
    private Long layoutId;
    private Long employeeCode;
    private Long tenantCode;
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
