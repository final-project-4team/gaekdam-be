package com.gaekdam.gaekdambe.communication_service.incident.command.domain.entity;

import com.gaekdam.gaekdambe.communication_service.incident.command.domain.IncidentSeverity;
import com.gaekdam.gaekdambe.communication_service.incident.command.domain.IncidentStatus;
import com.gaekdam.gaekdambe.communication_service.incident.command.domain.IncidentType;
import com.gaekdam.gaekdambe.communication_service.inquiry.command.domain.entity.Inquiry;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "incident", indexes = {
        @Index(name = "IDX_incident_property", columnList = "property_code"),
        @Index(name = "IDX_incident_status", columnList = "incident_status"),
        @Index(name = "IDX_incident_inquiry", columnList = "inquiry_code"),
        @Index(name = "idx_incident_property_status", columnList = "property_code, incident_status, incident_code")
})
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "incident_code", nullable = false)
    private Long incidentCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_code")
    private Inquiry inquiry; // 문의 참조(선택)

    @Column(name = "employee_code", nullable = false)
    private Long employeeCode; // 책임자

    @Column(name = "incident_title", nullable = false, length = 200)
    private String incidentTitle;

    @Column(name = "incident_summary", length = 500)
    private String incidentSummary;

    @Lob
    @Column(name = "incident_content", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String incidentContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 10)
    private IncidentSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "incident_type", nullable = false, length = 50)
    private IncidentType incidentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "incident_status", nullable = false, length = 30)
    private IncidentStatus incidentStatus;

    @Column(name = "occurred_at")
    private LocalDateTime occurredAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "property_code", nullable = false)
    private Long propertyCode;

    public static Incident create(
            Long propertyCode,
            Long employeeCode,
            String title,
            String summary,
            String content,
            IncidentType type,
            IncidentSeverity severity,
            LocalDateTime occurredAt,
            Inquiry inquiry) {
        LocalDateTime now = LocalDateTime.now();

        return Incident.builder()
                .propertyCode(propertyCode)
                .employeeCode(employeeCode)
                .incidentTitle(title)
                .incidentSummary(summary)
                .incidentContent(content)
                .incidentType(type)
                .severity(severity == null ? IncidentSeverity.MEDIUM : severity)
                .incidentStatus(IncidentStatus.IN_PROGRESS)
                .occurredAt(occurredAt)
                .inquiry(inquiry)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void changeOwner(Long employeeCode) {
        this.employeeCode = employeeCode;
        this.updatedAt = LocalDateTime.now();
    }

    public void close() {
        this.incidentStatus = IncidentStatus.CLOSED;
        this.updatedAt = LocalDateTime.now();
    }

    public void linkInquiry(Inquiry inquiry) {
        this.inquiry = inquiry;
        this.updatedAt = LocalDateTime.now();
    }

    public void unlinkInquiry() {
        this.inquiry = null;
        this.updatedAt = LocalDateTime.now();
    }
}
