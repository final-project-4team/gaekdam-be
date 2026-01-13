package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.request;

import lombok.Data;

@Data
public class ReportLayoutTemplateRequest {
    private String layoutTemplateId;
    private Long layoutId;
    private Long templateId;
    private Long createdBy;
    private String displayName;
    private Integer sortOrder;
    private Boolean isActive;
}
