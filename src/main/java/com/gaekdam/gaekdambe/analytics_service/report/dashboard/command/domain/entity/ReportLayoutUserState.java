package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@IdClass(ReportLayoutUserStateId.class)
@Entity
@Table(name = "ReportLayoutUserState")
@Data
public class ReportLayoutUserState {

    @Id
    @Column(name = "layout_id", nullable = false)
    private Long layoutId;

    @Id
    @Column(name = "employee_code", nullable = false)
    private Long employeeCode;

    @Column(name = "state_json", nullable = false, columnDefinition = "JSON")
    private String stateJson;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
