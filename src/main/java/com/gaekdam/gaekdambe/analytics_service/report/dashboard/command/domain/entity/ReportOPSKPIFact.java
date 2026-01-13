package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReportOPSKPIFact {
    private LocalDate kpiDate;
    private Long hotelGroupCode;
    private BigDecimal occupancyRate;
    private Integer remainingRooms;
    private Integer checkinCount;
    private Integer checkoutCount;
    private Integer plannedCheckinCount;
    private Integer plannedCheckoutCount;
    private BigDecimal adr;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
