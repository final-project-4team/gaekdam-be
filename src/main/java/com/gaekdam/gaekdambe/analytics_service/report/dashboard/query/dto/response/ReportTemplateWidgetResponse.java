package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReportTemplateWidgetResponse {
    private String templateWidgetId;
    private Long templateId;
    private String widgetType;
    private String title;
    private String datasetType;
    private String metricKey;
    private String dimensionKey;
    private String defaultPeriod;
    private Integer defaultSortOrder;
    private String optionsJson;
    private String defaultFilterJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
