package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReportCUSTCountryFact {
    private LocalDate kpiDate;
    private Long hotelGroupCode;
    private String countryCode;
    private Integer guestCount;
    private BigDecimal revenueAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
