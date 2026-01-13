package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReportLayoutWidgetResponse {
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
