package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity;

import java.time.LocalDateTime;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.VisibilityScope;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "report_layout")
@Data
public class ReportLayout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "layout_id", nullable = false)
    private Long layoutId;

    @Column(name = "employee_code", nullable = false)
    private Long employeeCode;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_default", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isDefault;

    @Column(name = "is_archived", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isArchived;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility_scope", nullable = false, length = 10)
    private VisibilityScope visibilityScope;

    @Column(name = "date_range_preset", nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'MONTH'")
    private String dateRangePreset;

    @Column(name = "default_filter_json", columnDefinition = "JSON")
    private String defaultFilterJson;

    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) this.createdAt = now;
        this.updatedAt = now;
        if (this.isDefault == null) this.isDefault = Boolean.FALSE;
        if (this.isArchived == null) this.isArchived = Boolean.FALSE;
        if (this.visibilityScope == null) this.visibilityScope = VisibilityScope.PRIVATE;
        if (this.dateRangePreset == null) this.dateRangePreset = "MONTH";
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}