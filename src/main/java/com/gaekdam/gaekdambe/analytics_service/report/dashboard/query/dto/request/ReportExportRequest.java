package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.request;

import lombok.Data;

@Data
public class ReportExportRequest {
    private String exportId;
    private Long layoutId;
    private Long layoutTemplateId;
    private Long layoutWidgetId;
    private Long requestedBy;
    private String exportFormat;
    private String exportScope;
    private String paramsJson;
}
