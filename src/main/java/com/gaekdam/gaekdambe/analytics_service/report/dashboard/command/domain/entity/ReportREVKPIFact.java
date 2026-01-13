package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReportREVKPIFact {
    private LocalDate kpiDate;
    private Long hotelGroupCode;
    private BigDecimal totalRevenue;
    private BigDecimal stayRevenue;
    private BigDecimal facilityRevenue;
    private BigDecimal adr;
    private Integer reservationCount;
    private BigDecimal cancelRate;
    private BigDecimal noShowRate;
    private Integer cancelCount;
    private Integer noShowCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
