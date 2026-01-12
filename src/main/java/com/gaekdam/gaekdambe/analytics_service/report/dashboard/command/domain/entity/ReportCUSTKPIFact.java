package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReportCUSTKPIFact {
    private LocalDate kpiDate;
    private Long hotelGroupCode;
    private BigDecimal repeatCustomerRate;
    private BigDecimal membershipRate;
    private BigDecimal personalRatio;
    private BigDecimal corporateRatio;
    private BigDecimal groupRatio;
    private BigDecimal foreignCustomerRate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
