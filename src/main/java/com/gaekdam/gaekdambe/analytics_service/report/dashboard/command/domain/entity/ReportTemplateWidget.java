package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "report_template_widget")
@Data
@Getter
@Setter
public class ReportTemplateWidget {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_widget_id")
    private Long templateWidgetId;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "widget_type", nullable = false, length = 10)
    private String widgetType; // KPI_CARD, LINE, BAR, TABLE, GAUGE

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "dataset_type", nullable = false, length = 5)
    private String datasetType; // CX/OPS/CUST/REV

    @Column(name = "metric_key", nullable = false, length = 100)
    private String metricKey;

    @Column(name = "dimension_key", length = 100)
    private String dimensionKey;

    @Column(name = "default_period", nullable = false, length = 10)
    private String defaultPeriod = "MONTH";

    @Column(name = "default_sort_order", nullable = false)
    private Integer defaultSortOrder = 0;

    @Column(name = "options_json", columnDefinition = "json")
    private String optionsJson;

    @Column(name = "default_filter_json", columnDefinition = "json")
    private String defaultFilterJson;

    @Column(name = "hotel_group_code", length = 50)
    private String hotelGroupCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        var now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        if (defaultPeriod == null) defaultPeriod = "MONTH";
        if (defaultSortOrder == null) defaultSortOrder = 0;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
