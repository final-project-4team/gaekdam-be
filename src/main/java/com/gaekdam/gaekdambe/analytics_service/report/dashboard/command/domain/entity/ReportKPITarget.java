package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReportKPITarget {
    private String targetId;
    private Long hotelGroupCode;
    private String kpiCode;
    private String periodType;
    private String periodValue;
    private BigDecimal targetValue;
    private BigDecimal warningThreshold;
    private BigDecimal dangerThreshold;
    private String seasonType;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
