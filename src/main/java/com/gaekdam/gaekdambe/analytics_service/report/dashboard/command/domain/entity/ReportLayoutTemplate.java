package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportLayoutTemplate {
    private String layoutTemplateId;
    private Long layoutId;
    private Long templateId;
    private Long createdBy;
    private String displayName;
    private Integer sortOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
