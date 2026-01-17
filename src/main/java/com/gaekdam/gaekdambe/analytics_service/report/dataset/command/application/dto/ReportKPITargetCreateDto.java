package com.gaekdam.gaekdambe.analytics_service.report.dataset.command.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ReportKPITargetCreateDto {

    @NotBlank
    private String targetId;

    @NotNull
    private Long hotelGroupCode;

    @NotBlank
    private String kpiCode;

    @NotBlank
    private String periodType;   // MONTH/YEAR

    @NotBlank
    private String periodValue;  // YYYY or YYYY-MM

    @NotNull
    private BigDecimal targetValue;

    private BigDecimal warningThreshold;
    private BigDecimal dangerThreshold;

    private String seasonType;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}
