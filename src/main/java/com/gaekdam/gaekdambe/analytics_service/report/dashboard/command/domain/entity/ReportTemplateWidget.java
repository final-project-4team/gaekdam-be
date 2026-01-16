package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "ReportTemplateWidget")
@Data
public class ReportTemplateWidget {

    @Id
    @Column(name = "template_widget_id", length = 255, nullable = false)
    private String templateWidgetId;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "widget_type", nullable = false, length = 10)
    private String widgetType;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "dataset_type", nullable = false, length = 5)
    private String datasetType;

    @Column(name = "metric_key", nullable = false, length = 100)
    private String metricKey;

    @Column(name = "dimension_key", length = 100)
    private String dimensionKey;

    @Column(name = "default_period", nullable = false, length = 10, columnDefinition = "VARCHAR(10) DEFAULT 'MONTH'")
    private String defaultPeriod;

    @Column(name = "default_sort_order", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer defaultSortOrder;

    @Column(name = "options_json", columnDefinition = "JSON")
    private String optionsJson;

    @Column(name = "default_filter_json", columnDefinition = "JSON")
    private String defaultFilterJson;

    @Column(name = "created_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) this.createdAt = now;
        if (this.defaultPeriod == null) this.defaultPeriod = "MONTH";
        if (this.defaultSortOrder == null) this.defaultSortOrder = 0;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
