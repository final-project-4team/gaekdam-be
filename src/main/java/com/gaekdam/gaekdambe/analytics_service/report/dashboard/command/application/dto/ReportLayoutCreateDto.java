package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.VisibilityScope;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportLayoutCreateDto {
    @NotNull
    private Long employeeCode;

    @NotBlank @Size(max = 100)
    private String name;

    private String description;
    private Boolean isDefault;

    @NotNull
    private VisibilityScope visibilityScope;

    private String dateRangePreset;

    // Json 형식 -> Object 타입으로 취급
    private Object defaultFilterJson;
}