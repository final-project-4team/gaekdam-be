package com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@IdClass(ReportCXKPIFactId.class)
@Entity
@Table(name = "ReportCXKPIFact")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportCXKPIFact {

    @Id
    @Column(name = "kpi_date", nullable = false)
    private LocalDate kpiDate;

    @Id
    @Column(name = "hotel_group_code", nullable = false)
    private Long hotelGroupCode;

    @Column(name = "total_requests", nullable = false)
    private Integer totalRequests;

    @Column(name = "total_inquiry_count", nullable = false)
    private Integer totalInquiryCount;

    @Column(name = "claim_count", nullable = false)
    private Integer claimCount;

    @Column(name = "unresolved_count", nullable = false)
    private Integer unresolvedCount;

    @Column(name = "resolved_count", nullable = false)
    private Integer resolvedCount;

    @Column(name = "avg_response_time", precision = 6, scale = 2)
    private BigDecimal avgResponseTime;

    @Column(name = "sla_violation_rate", precision = 5, scale = 2)
    private BigDecimal slaViolationRate;

    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
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
        this.updatedAt = LocalDateTime.now();
    }
}
