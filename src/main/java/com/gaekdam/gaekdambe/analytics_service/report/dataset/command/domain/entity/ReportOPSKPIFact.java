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

@IdClass(ReportOPSKPIFactId.class)
@Entity
@Table(name = "ReportOPSKPIFact")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportOPSKPIFact {

    @Id
    @Column(name = "kpi_date", nullable = false)
    private LocalDate kpiDate;

    @Id
    @Column(name = "hotel_group_code", nullable = false)
    private Long hotelGroupCode;

    @Column(name = "occupancy_rate", precision = 5, scale = 2)
    private BigDecimal occupancyRate;

    @Column(name = "remaining_rooms")
    private Integer remainingRooms;

    @Column(name = "checkin_count")
    private Integer checkinCount;

    @Column(name = "checkout_count")
    private Integer checkoutCount;

    @Column(name = "planned_checkin_count", columnDefinition = "INT DEFAULT 0")
    private Integer plannedCheckinCount;

    @Column(name = "planned_checkout_count", columnDefinition = "INT DEFAULT 0")
    private Integer plannedCheckoutCount;

    @Column(name = "adr", precision = 10, scale = 2)
    private BigDecimal adr;

    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) this.createdAt = now;
        this.updatedAt = now;
        if (this.plannedCheckinCount == null) this.plannedCheckinCount = 0;
        if (this.plannedCheckoutCount == null) this.plannedCheckoutCount = 0;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
