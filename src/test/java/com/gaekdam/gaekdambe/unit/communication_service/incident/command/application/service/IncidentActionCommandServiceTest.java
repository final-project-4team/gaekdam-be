package com.gaekdam.gaekdambe.unit.communication_service.incident.command.application.service;

import com.gaekdam.gaekdambe.communication_service.incident.command.application.dto.request.IncidentActionCreateRequest;
import com.gaekdam.gaekdambe.communication_service.incident.command.application.service.IncidentActionCommandService;
import com.gaekdam.gaekdambe.communication_service.incident.command.domain.entity.Incident;
import com.gaekdam.gaekdambe.communication_service.incident.command.domain.entity.IncidentActionHistory;
import com.gaekdam.gaekdambe.communication_service.incident.command.infrastructure.repository.IncidentActionHistoryRepository;
import com.gaekdam.gaekdambe.communication_service.incident.command.infrastructure.repository.IncidentRepository;
import com.gaekdam.gaekdambe.communication_service.incident.query.mapper.IncidentActionMapper;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class IncidentActionCommandServiceTest {

    private IncidentActionHistoryRepository historyRepository;
    private IncidentRepository incidentRepository;
    private IncidentActionMapper incidentActionMapper;
    private EntityManager entityManager;

    private IncidentActionCommandService service;

    @BeforeEach
    void setUp() {
        historyRepository = mock(IncidentActionHistoryRepository.class);
        incidentRepository = mock(IncidentRepository.class);
        incidentActionMapper = mock(IncidentActionMapper.class);
        entityManager = mock(EntityManager.class);

        service = new IncidentActionCommandService(
                historyRepository,
                incidentRepository,
                incidentActionMapper,
                entityManager
        );
    }

    @Test
    @DisplayName("createAction: loginId로 직원코드 못찾으면 INVALID_REQUEST")
    void createAction_employeeNotFound_thenThrow() {
        // given
        Long hotelGroupCode = 1L;
        String loginId = "tester";
        Long incidentCode = 10L;
        IncidentActionCreateRequest req = mock(IncidentActionCreateRequest.class);

        when(incidentActionMapper.findEmployeeCodeByLoginId(hotelGroupCode, loginId)).thenReturn(null);

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.createAction(hotelGroupCode, loginId, incidentCode, req),
                CustomException.class
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
        assertThat(ex.getMessage()).contains("직원 정보를 찾을 수 없습니다.");

        verify(incidentActionMapper).findEmployeeCodeByLoginId(hotelGroupCode, loginId);
        verifyNoMoreInteractions(incidentActionMapper);
        verifyNoInteractions(incidentRepository, entityManager, historyRepository);
    }

    @Test
    @DisplayName("createAction: incident 없으면 INVALID_REQUEST")
    void createAction_incidentNotFound_thenThrow() {
        // given
        Long hotelGroupCode = 1L;
        String loginId = "tester";
        Long incidentCode = 10L;
        Long employeeCode = 99L;

        IncidentActionCreateRequest req = mock(IncidentActionCreateRequest.class);

        when(incidentActionMapper.findEmployeeCodeByLoginId(hotelGroupCode, loginId)).thenReturn(employeeCode);
        when(incidentRepository.findById(incidentCode)).thenReturn(Optional.empty());

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.createAction(hotelGroupCode, loginId, incidentCode, req),
                CustomException.class
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
        assertThat(ex.getMessage()).contains("존재하지 않는 사건/사고입니다.");

        verify(incidentActionMapper).findEmployeeCodeByLoginId(hotelGroupCode, loginId);
        verify(incidentRepository).findById(incidentCode);
        verifyNoMoreInteractions(incidentActionMapper, incidentRepository);
        verifyNoInteractions(entityManager, historyRepository);
    }

    @Test
    @DisplayName("createAction: 정상 -> actionContent trim 후 저장, historyCode 반환")
    void createAction_success_trimAndSave() {
        // given
        Long hotelGroupCode = 1L;
        String loginId = "tester";
        Long incidentCode = 10L;
        Long writerEmployeeCode = 99L;

        IncidentActionCreateRequest req = mock(IncidentActionCreateRequest.class);
        when(req.getActionContent()).thenReturn("  조치내용  ");

        when(incidentActionMapper.findEmployeeCodeByLoginId(hotelGroupCode, loginId))
                .thenReturn(writerEmployeeCode);

        when(incidentRepository.findById(incidentCode))
                .thenReturn(Optional.of(mock(Incident.class)));

        Incident incidentRef = mock(Incident.class);
        when(entityManager.getReference(Incident.class, incidentCode)).thenReturn(incidentRef);

        IncidentActionHistory saved = mock(IncidentActionHistory.class);
        when(saved.getIncidentActionHistoryCode()).thenReturn(777L);
        when(historyRepository.save(any(IncidentActionHistory.class))).thenReturn(saved);

        // when
        Long historyCode = service.createAction(hotelGroupCode, loginId, incidentCode, req);

        // then
        assertThat(historyCode).isEqualTo(777L);

        // save로 들어간 히스토리 객체 내용 확인 (trim 되었는지)
        var captor = org.mockito.ArgumentCaptor.forClass(IncidentActionHistory.class);
        verify(historyRepository).save(captor.capture());

        IncidentActionHistory arg = captor.getValue();
        assertThat(arg.getEmployeeCode()).isEqualTo(writerEmployeeCode);
        assertThat(arg.getActionContent()).isEqualTo("조치내용");
        assertThat(arg.getIncident()).isSameAs(incidentRef);

        verify(incidentActionMapper).findEmployeeCodeByLoginId(hotelGroupCode, loginId);
        verify(incidentRepository).findById(incidentCode);
        verify(entityManager).getReference(Incident.class, incidentCode);

        verifyNoMoreInteractions(incidentActionMapper, incidentRepository, entityManager, historyRepository);
        verify(req).getActionContent();
        verifyNoMoreInteractions(req);
    }
}
