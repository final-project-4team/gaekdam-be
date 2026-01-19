package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportLayoutTemplateUpdateDto {
    private String displayName;
    private Integer sortOrder;
    private Boolean isActive;
}
