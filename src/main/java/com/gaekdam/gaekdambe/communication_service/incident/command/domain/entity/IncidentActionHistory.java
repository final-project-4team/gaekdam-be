package com.gaekdam.gaekdambe.communication_service.incident.command.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "incident_action_history",
        indexes = {
                @Index(name = "IDX_incident_action_history_incident", columnList = "incident_code"),
                @Index(name = "IDX_incident_action_history_created_at", columnList = "created_at")
        }
)
public class IncidentActionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "incident_action_history_code", nullable = false)
    private Long incidentActionHistoryCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_code", nullable = false)
    private Incident incident;

    @Column(name = "employee_code", nullable = false)
    private Long employeeCode;

    @Lob
    @Column(name = "action_content", nullable = false)
    private String actionContent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private IncidentActionHistory(Incident incident, Long employeeCode, String actionContent, LocalDateTime now) {
        this.incident = incident;
        this.employeeCode = employeeCode;
        this.actionContent = actionContent;
        this.createdAt = now;
    }

    public static IncidentActionHistory create(Incident incident, Long employeeCode, String actionContent) {
        return new IncidentActionHistory(incident, employeeCode, actionContent, LocalDateTime.now());
    }
}
