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

@IdClass(ReportCUSTKPIFactId.class)
@Entity
@Table(name = "ReportCUSTKPIFact")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportCUSTKPIFact {

    @Id
    @Column(name = "kpi_date", nullable = false)
    private LocalDate kpiDate;

    @Id
    @Column(name = "hotel_group_code", nullable = false)
    private Long hotelGroupCode;

    @Column(name = "repeat_customer_rate", precision = 5, scale = 2)
    private BigDecimal repeatCustomerRate;

    @Column(name = "membership_rate", precision = 5, scale = 2)
    private BigDecimal membershipRate;

    @Column(name = "personal_ratio", precision = 5, scale = 2)
    private BigDecimal personalRatio;

    @Column(name = "corporate_ratio", precision = 5, scale = 2)
    private BigDecimal corporateRatio;

    @Column(name = "group_ratio", precision = 5, scale = 2)
    private BigDecimal groupRatio;

    @Column(name = "foreign_customer_rate", precision = 5, scale = 2)
    private BigDecimal foreignCustomerRate;

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
