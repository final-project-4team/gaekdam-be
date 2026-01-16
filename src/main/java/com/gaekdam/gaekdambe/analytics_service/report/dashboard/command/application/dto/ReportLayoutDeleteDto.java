package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportLayoutDeleteDto {
        
    @NotNull
    private Long layoutId;
}
