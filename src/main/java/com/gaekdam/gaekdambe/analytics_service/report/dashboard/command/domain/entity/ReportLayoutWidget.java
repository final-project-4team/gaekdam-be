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

@Entity
@Table(name = "ReportLayoutWidget")
@Data
public class ReportLayoutWidget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "layout_widget_id", nullable = false)
    private Long layoutWidgetId;

    @Column(name = "layout_id", nullable = false)
    private Long layoutId;

    @Column(name = "layout_template_id")
    private Long layoutTemplateId;

    @Column(name = "template_widget_id")
    private Long templateWidgetId;

    @Column(name = "widget_type", length = 10)
    private String widgetType;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "x", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer x;

    @Column(name = "y", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer y;

    @Column(name = "w", nullable = false, columnDefinition = "INT DEFAULT 4")
    private Integer w;

    @Column(name = "h", nullable = false, columnDefinition = "INT DEFAULT 4")
    private Integer h;

    @Column(name = "options_json", columnDefinition = "JSON")
    private String optionsJson;

    @Column(name = "filter_json", columnDefinition = "JSON")
    private String filterJson;

    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) this.createdAt = now;
        this.updatedAt = now;
        if (this.x == null) this.x = 0;
        if (this.y == null) this.y = 0;
        if (this.w == null) this.w = 4;
        if (this.h == null) this.h = 4;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
