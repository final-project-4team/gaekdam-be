package com.gaekdam.gaekdambe.unit.analytics_service.report.dataset.command.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.application.service.ReportKPITargetSaveService;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPITarget;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPITargetId;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.infrastructure.repository.ReportKPITargetRepository;

class ReportKPITargetSaveServiceTest {

    @Mock
    ReportKPITargetRepository targetRepo;

    private ReportKPITargetSaveService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ReportKPITargetSaveService(targetRepo);
    }

    @Test
    void saveSingleTarget_createsNewEntity_whenIdIsNull() {
        ReportKPITarget t = new ReportKPITarget();
        t.setKpiCode("K1");
        t.setPeriodType("YEAR");
        t.setPeriodValue("2024");

        service.saveSingleTarget(t);

        // verify save called and timestamps set
        verify(targetRepo).save(any(ReportKPITarget.class));
        assertThat(t.getCreatedAt()).isNotNull();
        assertThat(t.getUpdatedAt()).isNotNull();
    }

    @Test
    void saveSingleTarget_updatesExisting_whenIdPresentAndFound() {
        ReportKPITarget t = new ReportKPITarget();
        ReportKPITargetId id = new ReportKPITargetId("T1", 123L);
        t.setId(id);
        t.setKpiCode("K1");
        t.setPeriodType("YEAR");
        t.setPeriodValue("2024");
        t.setTargetValue(null);

        ReportKPITarget existing = new ReportKPITarget();
        existing.setId(id);
        existing.setKpiCode("OLD");
        existing.setCreatedAt(null);

        when(targetRepo.findById(id)).thenReturn(Optional.of(existing));

        service.saveSingleTarget(t);

        // existing should be updated and saved
        verify(targetRepo).save(existing);
        assertThat(existing.getKpiCode()).isEqualTo("K1");
        assertThat(existing.getUpdatedAt()).isNotNull();
        assertThat(existing.getCreatedAt()).isNotNull();
    }
}
