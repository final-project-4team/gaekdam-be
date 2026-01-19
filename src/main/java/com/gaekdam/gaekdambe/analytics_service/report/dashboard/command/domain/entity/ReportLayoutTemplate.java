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
@Table(name = "report_layout_template")
@Data
public class ReportLayoutTemplate {
    
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "layout_template_id")
  private Long layoutTemplateId;

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

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  void prePersist() {
    var now = LocalDateTime.now();
    if (createdAt == null) createdAt = now;
    updatedAt = now;
    if (isActive == null) isActive = true;
    if (sortOrder == null) sortOrder = 0;
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = LocalDateTime.now();
  }  
}
