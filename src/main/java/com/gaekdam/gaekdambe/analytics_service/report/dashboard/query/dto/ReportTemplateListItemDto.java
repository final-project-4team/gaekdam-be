package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportTemplateListItemDto {
    private Long templateId;
    private String templateType;
    private String templateName;
    private String templateDesc;
    private Boolean isActive;
}
