package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportLayoutTemplateCreateDto {
    @NotNull
    private Long templateId;

    @NotBlank
    private String displayName;

    private Integer sortOrder;
}
