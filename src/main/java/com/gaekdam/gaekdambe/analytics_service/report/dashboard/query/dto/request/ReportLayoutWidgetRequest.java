package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.request;

import lombok.Data;

@Data
public class ReportLayoutWidgetRequest {
    private Long layoutWidgetId;
    private Long layoutId;
    private Long layoutTemplateId;
    private Long templateWidgetId;
    private String widgetType;
    private String title;
    private Integer x;
    private Integer y;
    private Integer w;
    private Integer h;
    private String optionsJson;
    private String filterJson;
}
