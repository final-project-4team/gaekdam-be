package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ReportKPICodeDim")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportKPICodeDim {
    @Id
    @Column(name = "kpi_code", length = 50)
    private String kpiCode;

    @Column(name = "kpi_name", nullable = false, length = 100)
    private String kpiName;

    @Column(name = "domain_type", nullable = false, length = 10)
    private String domainType;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "calc_rule_json", columnDefinition = "JSON")
    private String calcRuleJson;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}