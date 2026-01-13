package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "ReportExport")
@Data
public class ReportExport {

    @Id
    @Column(name = "export_id", length = 255, nullable = false)
    private String exportId;

    @Column(name = "layout_id", nullable = false)
    private Long layoutId;

    @Column(name = "layout_template_id")
    private Long layoutTemplateId;

    @Column(name = "layout_widget_id")
    private Long layoutWidgetId;

    @Column(name = "requested_by", nullable = false)
    private Long requestedBy;

    @Column(name = "export_format", nullable = false, length = 5)
    private String exportFormat;

    @Column(name = "export_scope", nullable = false, length = 10, columnDefinition = "VARCHAR(10) DEFAULT 'LAYOUT'")
    private String exportScope;

    @Column(name = "params_json", nullable = false, columnDefinition = "JSON")
    private String paramsJson;

    @Column(name = "success_yn", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean successYn;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.exportScope == null) this.exportScope = "LAYOUT";
        if (this.successYn == null) this.successYn = Boolean.TRUE;
    }
}
