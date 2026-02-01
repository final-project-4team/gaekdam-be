package com.gaekdam.gaekdambe.communication_service.incident.command.application.service;

import com.gaekdam.gaekdambe.communication_service.incident.command.application.dto.request.IncidentCreateRequest;
import com.gaekdam.gaekdambe.communication_service.incident.command.domain.IncidentStatus;
import com.gaekdam.gaekdambe.communication_service.incident.command.domain.entity.Incident;
import com.gaekdam.gaekdambe.communication_service.incident.command.infrastructure.repository.IncidentRepository;
import com.gaekdam.gaekdambe.communication_service.inquiry.command.domain.entity.Inquiry;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.AuditLog;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
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
    @AuditLog(details = "'사건사고 등록 제목 : '+#request.incidentTitle", type = PermissionTypeKey.INCIDENT_CREATE)
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

    @Transactional
    public void closeIncident(Long incidentCode) {

        Incident incident = incidentRepository.findById(incidentCode)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST, "존재하지 않는 사건/사고입니다."));

        // 이미 종결이면 그냥 리턴 or 예외
        if (incident.getIncidentStatus() == IncidentStatus.CLOSED) {
            return;
        }

        incident.close(); // 엔티티 메서드로 상태 변경
    }

}
