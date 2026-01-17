package com.gaekdam.gaekdambe.analytics_service.report.dataset.command.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ReportKPITargetUpdateDto {

    private String periodType;
    private String periodValue;

    private BigDecimal targetValue;
    private BigDecimal warningThreshold;
    private BigDecimal dangerThreshold;
    private String seasonType;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}
