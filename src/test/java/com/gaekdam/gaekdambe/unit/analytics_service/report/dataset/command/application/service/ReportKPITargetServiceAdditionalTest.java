package com.gaekdam.gaekdambe.unit.analytics_service.report.dataset.command.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;

import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.application.dto.ReportKPITargetCreateDto;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.application.dto.ReportKPITargetUpdateDto;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.application.service.ReportKPITargetService;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPITarget;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPITargetId;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.infrastructure.repository.ReportKPICodeDimRepository;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.infrastructure.repository.ReportKPITargetRepository;
import com.gaekdam.gaekdambe.global.exception.CustomException;

class ReportKPITargetServiceAdditionalTest {

    @Mock
    ReportKPITargetRepository targetRepo;
    @Mock
    ReportKPICodeDimRepository kpiRepo;

    private ReportKPITargetService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ReportKPITargetService(targetRepo, kpiRepo);
    }

    @Test
    void create_duplicate_throws() {
        ReportKPITargetCreateDto dto = ReportKPITargetCreateDto.builder()
            .targetId("T1")
            .hotelGroupCode(10L)
            .kpiCode("K1")
            .periodType("YEAR")
            .periodValue("2024")
            .build();

        when(kpiRepo.existsById("K1")).thenReturn(true);
        when(targetRepo.existsById(any(ReportKPITargetId.class))).thenReturn(true);

        assertThatThrownBy(() -> service.create(dto)).isInstanceOf(CustomException.class);
    }

    @Test
    void create_save_throws_dataIntegrity_wrapped() {
        ReportKPITargetCreateDto dto = ReportKPITargetCreateDto.builder()
            .targetId("T2")
            .hotelGroupCode(20L)
            .kpiCode("K2")
            .periodType("YEAR")
            .periodValue("2023")
            .build();

        when(kpiRepo.existsById("K2")).thenReturn(true);
        when(targetRepo.existsById(any(ReportKPITargetId.class))).thenReturn(false);
        doThrow(new DataIntegrityViolationException("fk")).when(targetRepo).save(any(ReportKPITarget.class));

        assertThatThrownBy(() -> service.create(dto)).isInstanceOf(CustomException.class);
    }

    @Test
    void create_month_valid_success() {
        ReportKPITargetCreateDto dto = ReportKPITargetCreateDto.builder()
            .targetId("T3")
            .hotelGroupCode(30L)
            .kpiCode("K3")
            .periodType("MONTH")
            .periodValue("2024-05")
            .targetValue(new BigDecimal("10"))
            .build();

        when(kpiRepo.existsById("K3")).thenReturn(true);
        when(targetRepo.existsById(any(ReportKPITargetId.class))).thenReturn(false);

        var id = service.create(dto);
        assertThat(id.getTargetId()).isEqualTo("T3");
        assertThat(id.getHotelGroupCode()).isEqualTo(30L);
    }

    @Test
    void update_invalid_period_throws() {
        ReportKPITargetUpdateDto dto = new ReportKPITargetUpdateDto();
        dto.setPeriodType("MONTH");
        dto.setPeriodValue("2024-13"); // invalid month

        // existing entity to satisfy findById
        ReportKPITargetId id = new ReportKPITargetId("X", 1L);
        ReportKPITarget existing = new ReportKPITarget();
        existing.setId(id);
        existing.setPeriodType("YEAR");
        existing.setPeriodValue("2023");

        when(targetRepo.findById(any(ReportKPITargetId.class))).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.update("X", 1L, dto)).isInstanceOf(CustomException.class);
    }

    @Test
    void delete_success_invokesRepository() {
        when(targetRepo.existsById(any(ReportKPITargetId.class))).thenReturn(true);

        service.delete("TDEL", 99L);

        verify(targetRepo).deleteById(any(ReportKPITargetId.class));
    }
}