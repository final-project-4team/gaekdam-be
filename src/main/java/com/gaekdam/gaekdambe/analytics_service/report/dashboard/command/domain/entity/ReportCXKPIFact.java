package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReportCXKPIFact {
    private LocalDate kpiDate;
    private Long hotelGroupCode;
    private Integer totalRequests;
    private Integer totalInquiryCount;
    private Integer claimCount;
    private Integer unresolvedCount;
    private Integer resolvedCount;
    private BigDecimal avgResponseTime;
    private BigDecimal slaViolationRate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
