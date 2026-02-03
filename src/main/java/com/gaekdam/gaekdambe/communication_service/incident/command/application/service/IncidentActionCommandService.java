package com.gaekdam.gaekdambe.communication_service.incident.command.application.service;

import com.gaekdam.gaekdambe.communication_service.incident.command.application.dto.request.IncidentActionCreateRequest;
import com.gaekdam.gaekdambe.communication_service.incident.command.domain.entity.Incident;
import com.gaekdam.gaekdambe.communication_service.incident.command.domain.entity.IncidentActionHistory;
import com.gaekdam.gaekdambe.communication_service.incident.command.infrastructure.repository.IncidentActionHistoryRepository;
import com.gaekdam.gaekdambe.communication_service.incident.command.infrastructure.repository.IncidentRepository;
import com.gaekdam.gaekdambe.communication_service.incident.query.mapper.IncidentActionMapper;
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
public class IncidentActionCommandService {

    private final IncidentActionHistoryRepository incidentActionHistoryRepository;
    private final IncidentRepository incidentRepository;
    private final IncidentActionMapper incidentActionMapper;
    private final EntityManager entityManager;

    @Transactional
    @AuditLog(details = "'조치 이력 내용 : '+#request.actionContent", type = PermissionTypeKey.INCIDENT_ACTION_CREATE)
    public Long createAction(Long hotelGroupCode, String loginId, Long incidentCode, IncidentActionCreateRequest request) {

        Long writerEmployeeCode = incidentActionMapper.findEmployeeCodeByLoginId(hotelGroupCode, loginId);
        if (writerEmployeeCode == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "직원 정보를 찾을 수 없습니다.");
        }

        incidentRepository.findById(incidentCode)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST, "존재하지 않는 사건/사고입니다."));

        Incident incidentRef = entityManager.getReference(Incident.class, incidentCode);

        IncidentActionHistory history = IncidentActionHistory.create(
                incidentRef,
                writerEmployeeCode,
                request.getActionContent().trim()
        );

        return incidentActionHistoryRepository.save(history).getIncidentActionHistoryCode();
    }
}
