package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ReportKPITarget")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportKPITarget {
    @Id
    @Column(name = "target_id", length = 255)
    private String targetId;

    @Column(name = "hotel_group_code", nullable = false)
    private Long hotelGroupCode;

    @Column(name = "kpi_code", nullable = false, length = 50)
    private String kpiCode;

    @Column(name = "period_type", nullable = false, length = 5)
    private String periodType;

    @Column(name = "period_value", nullable = false, length = 7)
    private String periodValue;

    @Column(name = "target_value", nullable = false, precision = 15, scale = 4)
    private BigDecimal targetValue;

    @Column(name = "warning_threshold", precision = 15, scale = 4)
    private BigDecimal warningThreshold;

    @Column(name = "danger_threshold", precision = 15, scale = 4)
    private BigDecimal dangerThreshold;

    @Column(name = "season_type", length = 5)
    private String seasonType;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
