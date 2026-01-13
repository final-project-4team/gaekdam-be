package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity;

import java.time.LocalDateTime;

import lombok.Data;

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
