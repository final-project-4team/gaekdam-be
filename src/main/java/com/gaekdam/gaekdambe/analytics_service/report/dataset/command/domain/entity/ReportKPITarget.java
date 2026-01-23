package com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ReportKPITarget")
@Getter
@Setter
@NoArgsConstructor
public class ReportKPITarget {

    @EmbeddedId
    private ReportKPITargetId id;

    @Column(name = "kpi_code", length = 50, nullable = false)
    private String kpiCode; // FK: ReportKPICodeDim.kpi_code (DB FK는 별도 추가 가능)

    @Column(name = "period_type", length = 5, nullable = false)
    private String periodType; // MONTH / YEAR

    @Column(name = "period_value", length = 7, nullable = false)
    private String periodValue; // YYYY or YYYY-MM

    // allow null targets when importing; change nullable=false -> true
    @Column(name = "target_value", precision = 15, scale = 4, nullable = true)
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

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) this.createdAt = now; // <-- 추가
        this.updatedAt = now;
    }
}