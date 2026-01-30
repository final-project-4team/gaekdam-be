package com.gaekdam.gaekdambe.unit.communication_service.incident.command.application.service;

import com.gaekdam.gaekdambe.communication_service.incident.command.application.dto.request.IncidentCreateRequest;
import com.gaekdam.gaekdambe.communication_service.incident.command.application.service.IncidentCommandService;
import com.gaekdam.gaekdambe.communication_service.incident.command.domain.IncidentSeverity;
import com.gaekdam.gaekdambe.communication_service.incident.command.domain.IncidentStatus;
import com.gaekdam.gaekdambe.communication_service.incident.command.domain.IncidentType;
import com.gaekdam.gaekdambe.communication_service.incident.command.domain.entity.Incident;
import com.gaekdam.gaekdambe.communication_service.incident.command.infrastructure.repository.IncidentRepository;
import com.gaekdam.gaekdambe.communication_service.inquiry.command.domain.entity.Inquiry;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class IncidentCommandServiceTest {

    private IncidentRepository incidentRepository;
    private EntityManager entityManager;
    private IncidentCommandService service;

    @BeforeEach
    void setUp() {
        incidentRepository = mock(IncidentRepository.class);
        entityManager = mock(EntityManager.class);
        service = new IncidentCommandService(incidentRepository, entityManager);
    }

    @Test
    @DisplayName("createIncident: inquiryCode 없으면 inquiry null로 생성 후 저장")
    void createIncident_success_withoutInquiry() {
        // given
        IncidentCreateRequest req = mock(IncidentCreateRequest.class);

        when(req.getInquiryCode()).thenReturn(null);
        when(req.getPropertyCode()).thenReturn(1L);
        when(req.getEmployeeCode()).thenReturn(10L);
        when(req.getIncidentTitle()).thenReturn("title");
        when(req.getIncidentSummary()).thenReturn("summary");
        when(req.getIncidentContent()).thenReturn("content");
        when(req.getIncidentType()).thenReturn(IncidentType.FACILITY);
        when(req.getSeverity()).thenReturn(null); // 기본값(MEDIUM) 확인용
        LocalDateTime occurredAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        when(req.getOccurredAt()).thenReturn(occurredAt);

        Incident saved = mock(Incident.class);
        when(saved.getIncidentCode()).thenReturn(100L);
        when(incidentRepository.save(any(Incident.class))).thenReturn(saved);

        // when
        Long incidentCode = service.createIncident(req);

        // then
        assertThat(incidentCode).isEqualTo(100L);

        var captor = org.mockito.ArgumentCaptor.forClass(Incident.class);
        verify(incidentRepository).save(captor.capture());

        Incident arg = captor.getValue();
        assertThat(arg.getPropertyCode()).isEqualTo(1L);
        assertThat(arg.getEmployeeCode()).isEqualTo(10L);
        assertThat(arg.getIncidentTitle()).isEqualTo("title");
        assertThat(arg.getIncidentSummary()).isEqualTo("summary");
        assertThat(arg.getIncidentContent()).isEqualTo("content");
        assertThat(arg.getIncidentType()).isEqualTo(IncidentType.FACILITY);
        assertThat(arg.getSeverity()).isEqualTo(IncidentSeverity.MEDIUM); // null이면 MEDIUM
        assertThat(arg.getIncidentStatus()).isEqualTo(IncidentStatus.IN_PROGRESS);
        assertThat(arg.getOccurredAt()).isEqualTo(occurredAt);
        assertThat(arg.getInquiry()).isNull();

        verifyNoInteractions(entityManager);
        verifyNoMoreInteractions(incidentRepository);
    }

    @Test
    @DisplayName("createIncident: inquiryCode 있으면 Inquiry reference로 링크 후 저장")
    void createIncident_success_withInquiryReference() {
        // given
        IncidentCreateRequest req = mock(IncidentCreateRequest.class);

        when(req.getInquiryCode()).thenReturn(55L);
        when(req.getPropertyCode()).thenReturn(1L);
        when(req.getEmployeeCode()).thenReturn(10L);
        when(req.getIncidentTitle()).thenReturn("title");
        when(req.getIncidentSummary()).thenReturn("summary");
        when(req.getIncidentContent()).thenReturn("content");
        when(req.getIncidentType()).thenReturn(IncidentType.FACILITY);
        when(req.getSeverity()).thenReturn(IncidentSeverity.HIGH);
        when(req.getOccurredAt()).thenReturn(null);

        Inquiry inquiryRef = mock(Inquiry.class);
        when(entityManager.getReference(Inquiry.class, 55L)).thenReturn(inquiryRef);

        Incident saved = mock(Incident.class);
        when(saved.getIncidentCode()).thenReturn(200L);
        when(incidentRepository.save(any(Incident.class))).thenReturn(saved);

        // when
        Long incidentCode = service.createIncident(req);

        // then
        assertThat(incidentCode).isEqualTo(200L);

        var captor = org.mockito.ArgumentCaptor.forClass(Incident.class);
        verify(incidentRepository).save(captor.capture());

        Incident arg = captor.getValue();
        assertThat(arg.getInquiry()).isSameAs(inquiryRef);
        assertThat(arg.getSeverity()).isEqualTo(IncidentSeverity.HIGH);

        verify(entityManager).getReference(Inquiry.class, 55L);
        verifyNoMoreInteractions(entityManager);
        verifyNoMoreInteractions(incidentRepository);
    }

    @Test
    @DisplayName("closeIncident: incident 없으면 INVALID_REQUEST")
    void closeIncident_notFound_thenThrow() {
        // given
        Long incidentCode = 10L;
        when(incidentRepository.findById(incidentCode)).thenReturn(Optional.empty());

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.closeIncident(incidentCode),
                CustomException.class
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
        assertThat(ex.getMessage()).contains("존재하지 않는 사건/사고입니다.");

        verify(incidentRepository).findById(incidentCode);
        verifyNoMoreInteractions(incidentRepository);
        verifyNoInteractions(entityManager);
    }

    @Test
    @DisplayName("closeIncident: 이미 CLOSED면 close() 호출 안 하고 리턴")
    void closeIncident_alreadyClosed_thenReturn() {
        // given
        Long incidentCode = 10L;
        Incident incident = mock(Incident.class);
        when(incident.getIncidentStatus()).thenReturn(IncidentStatus.CLOSED);
        when(incidentRepository.findById(incidentCode)).thenReturn(Optional.of(incident));

        // when
        service.closeIncident(incidentCode);

        // then
        verify(incidentRepository).findById(incidentCode);
        verify(incident).getIncidentStatus();
        verify(incident, never()).close();
        verifyNoMoreInteractions(incidentRepository, incident);
        verifyNoInteractions(entityManager);
    }

    @Test
    @DisplayName("closeIncident: CLOSED가 아니면 close() 호출")
    void closeIncident_notClosed_thenCloseCalled() {
        // given
        Long incidentCode = 10L;
        Incident incident = mock(Incident.class);
        when(incident.getIncidentStatus()).thenReturn(IncidentStatus.IN_PROGRESS);
        when(incidentRepository.findById(incidentCode)).thenReturn(Optional.of(incident));

        // when
        service.closeIncident(incidentCode);

        // then
        verify(incidentRepository).findById(incidentCode);
        verify(incident).getIncidentStatus();
        verify(incident).close();
        verifyNoMoreInteractions(incidentRepository, incident);
        verifyNoInteractions(entityManager);
    }
}
