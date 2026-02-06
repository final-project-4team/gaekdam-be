package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.VisibilityScope;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportLayoutUpdateDto {
    @NotNull
    private Long layoutId;

    private String name;
    private String description;
    private Boolean isDefault;
    private Boolean isArchived;
    private VisibilityScope visibilityScope;
    private String dateRangePreset;

    // Json 형식 -> Object 타입으로 취급
    private Object defaultFilterJson;
}