package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity;

import java.time.LocalDateTime;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.ReportTemplateType;

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
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "report_template")
@Data
@Getter
@Setter
public class ReportTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id", nullable = false)
    private Long templateId;

    // "라이브러리 템플릿" 생성자(시드/관리자). 운영에서 고정이면 SYSTEM 같은 값으로 넣어도 됨.
    @Column(name = "employee_code", nullable = false)
    private Long employeeCode;

    // 5종 고정이면 enum 
    @Enumerated(EnumType.STRING)
    @Column(name = "template_type", nullable = false, length = 30)
    private ReportTemplateType templateType;

    // 라이브러리 기본명(고정)
    @Column(name = "template_name", nullable = false, length = 100)
    private String templateName;

    @Column(name = "template_desc", length = 255)
    private String templateDesc;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "version", nullable = false)
    private Integer version = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = (this.createdAt == null) ? now : this.createdAt;
        this.updatedAt = now;
        if (this.isActive == null) this.isActive = true;
        if (this.version == null) this.version = 1;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
