package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReportTemplateResponse {
    private Long templateId;
    private Long employeeCode;
    private String templateName;
    private String templateDesc;
    private Boolean isActive;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
