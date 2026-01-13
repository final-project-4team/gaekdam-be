package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.request;

import lombok.Data;

@Data
public class ReportTemplateWidgetRequest {
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
}
