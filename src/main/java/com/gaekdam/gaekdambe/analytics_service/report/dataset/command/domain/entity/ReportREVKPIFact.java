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

@IdClass(ReportREVKPIFactId.class)
@Entity
@Table(name = "ReportREVKPIFact")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportREVKPIFact {

    @Id
    @Column(name = "kpi_date", nullable = false)
    private LocalDate kpiDate;

    @Id
    @Column(name = "hotel_group_code", nullable = false)
    private Long hotelGroupCode;

    @Column(name = "total_revenue", precision = 15, scale = 2)
    private BigDecimal totalRevenue;

    @Column(name = "stay_revenue", precision = 15, scale = 2)
    private BigDecimal stayRevenue;

    @Column(name = "facility_revenue", precision = 15, scale = 2)
    private BigDecimal facilityRevenue;

    @Column(name = "adr", precision = 10, scale = 2)
    private BigDecimal adr;

    @Column(name = "reservation_count")
    private Integer reservationCount;

    @Column(name = "cancel_rate", precision = 5, scale = 2)
    private BigDecimal cancelRate;

    @Column(name = "no_show_rate", precision = 5, scale = 2)
    private BigDecimal noShowRate;

    @Column(name = "cancel_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer cancelCount;

    @Column(name = "no_show_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer noShowCount;

    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) this.createdAt = now;
        this.updatedAt = now;
        if (this.cancelCount == null) this.cancelCount = 0;
        if (this.noShowCount == null) this.noShowCount = 0;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
