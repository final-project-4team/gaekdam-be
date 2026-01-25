package com.gaekdam.gaekdambe.communication_service.incident.command.application.service;

import com.gaekdam.gaekdambe.communication_service.incident.command.application.dto.request.IncidentCreateRequest;
import com.gaekdam.gaekdambe.communication_service.incident.command.domain.entity.Incident;
import com.gaekdam.gaekdambe.communication_service.incident.command.infrastructure.repository.IncidentRepository;
import com.gaekdam.gaekdambe.communication_service.inquiry.command.domain.entity.Inquiry;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IncidentCommandService {

    private final IncidentRepository incidentRepository;
    private final EntityManager entityManager;

    @Transactional
    public Long createIncident(IncidentCreateRequest request) {

        Inquiry inquiry = null;
        if (request.getInquiryCode() != null) {
            inquiry = entityManager.getReference(Inquiry.class, request.getInquiryCode());
        }

        Incident incident = Incident.create(
                request.getPropertyCode(),
                request.getEmployeeCode(),
                request.getIncidentTitle(),
                request.getIncidentSummary(),
                request.getIncidentContent(),
                request.getIncidentType(),
                request.getSeverity(),
                request.getOccurredAt(),
                inquiry
        );

        return incidentRepository.save(incident).getIncidentCode();
    }
}
