package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportTemplateWidgetResponseDto {
    private Long templateWidgetId;
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
}
