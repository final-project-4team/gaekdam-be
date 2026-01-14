package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "ReportLayoutTemplate")
@Data
public class ReportLayoutTemplate {
    @Id
    @Column(name = "layout_template_id", length = 255, nullable = false)
    private String layoutTemplateId;

    @Column(name = "layout_id", nullable = false)
    private Long layoutId;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "is_active", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.isActive == null) this.isActive = Boolean.TRUE;
        if (this.sortOrder == null) this.sortOrder = 0;
    }
}
