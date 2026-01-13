package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.request;

import lombok.Data;

@Data
public class ReportTemplateRequest {
    private Long templateId;
    private Long employeeCode;
    private String templateName;
    private String templateDesc;
    private Boolean isActive;
    private Integer version;
}
