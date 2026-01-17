package com.gaekdam.gaekdambe.analytics_service.report.dataset.command.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ReportKPITargetResponseDto {
    private String targetId;
    private Long hotelGroupCode;
    private String kpiCode;
    private String periodType;
    private String periodValue;
    private String targetValue;
    private String warningThreshold;
    private String dangerThreshold;
    private String seasonType;
    private String effectiveFrom;
    private String effectiveTo;
    private String createdAt;
    private String updatedAt;
}
