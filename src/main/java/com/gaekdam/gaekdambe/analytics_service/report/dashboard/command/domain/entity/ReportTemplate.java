package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReportTemplate {
    private Long templateId;
    private Long employeeCode;
    private String templateName;
    private String templateDesc;
    private Boolean isActive;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
