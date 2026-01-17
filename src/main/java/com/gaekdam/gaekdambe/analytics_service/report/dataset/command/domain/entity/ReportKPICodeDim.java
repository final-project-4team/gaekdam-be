package com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ReportKPICodeDim")
@Getter
@Setter
@NoArgsConstructor
public class ReportKPICodeDim {

    @Id
    @Column(name = "kpi_code", length = 50, nullable = false)
    private String kpiCode;

    @Column(name = "kpi_name", length = 100, nullable = false)
    private String kpiName;

    @Column(name = "domain_type", length = 10, nullable = false)
    private String domainType; // "CX", "OPS", "CUST", "REV"

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "description", length = 255)
    private String description;

    // MariaDB JSON -> 일단 String으로 (가장 안정적)
    @Column(name = "calc_rule_json", columnDefinition = "json")
    private String calcRuleJson;

    // tinyint(1) 매핑 (Hibernate가 Boolean <-> tinyint로 보통 처리)
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        if (isActive == null) isActive = true;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
