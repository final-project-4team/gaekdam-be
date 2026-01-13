package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReportExport {
    private String exportId;
    private Long layoutId;
    private Long layoutTemplateId;
    private Long layoutWidgetId;
    private Long requestedBy;
    private String exportFormat;
    private String exportScope;
    private String paramsJson;
    private Boolean successYn;
    private String failureReason;
    private LocalDateTime createdAt;
}
