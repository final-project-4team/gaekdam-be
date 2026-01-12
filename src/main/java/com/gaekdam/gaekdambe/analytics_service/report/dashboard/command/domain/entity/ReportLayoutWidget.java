package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportLayoutWidget {
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
