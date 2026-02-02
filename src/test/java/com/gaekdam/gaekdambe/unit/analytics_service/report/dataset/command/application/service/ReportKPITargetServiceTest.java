package com.gaekdam.gaekdambe.unit.analytics_service.report.dataset.command.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.application.dto.ReportKPITargetCreateDto;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.application.dto.ReportKPITargetUpdateDto;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.application.service.ReportKPITargetService;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPITarget;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPITargetId;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.infrastructure.repository.ReportKPICodeDimRepository;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.infrastructure.repository.ReportKPITargetRepository;

class ReportKPITargetServiceTest {

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
    void create_success_returnsId() {
        ReportKPITargetCreateDto dto = ReportKPITargetCreateDto.builder()
            .targetId("T1")
            .hotelGroupCode(10L)
            .kpiCode("K1")
            .periodType("YEAR")
            .periodValue("2024")
            .targetValue(new BigDecimal("100"))
            .build();

        when(kpiRepo.existsById("K1")).thenReturn(true);
        when(targetRepo.existsById(any(ReportKPITargetId.class))).thenReturn(false);

        var id = service.create(dto);
        assertThat(id).isNotNull();
        assertThat(id.getTargetId()).isEqualTo("T1");
        assertThat(id.getHotelGroupCode()).isEqualTo(10L);
    }

    @Test
    void create_missingKpi_throws() {
        ReportKPITargetCreateDto dto = ReportKPITargetCreateDto.builder()
            .targetId("T1")
            .hotelGroupCode(10L)
            .kpiCode("KX")
            .periodType("YEAR")
            .periodValue("2024")
            .targetValue(new BigDecimal("100"))
            .build();

        when(kpiRepo.existsById("KX")).thenReturn(false);

        assertThatThrownBy(() -> service.create(dto))
            .isInstanceOf(com.gaekdam.gaekdambe.global.exception.CustomException.class);
    }

    @Test
    void create_invalidPeriod_throws() {
        ReportKPITargetCreateDto dto = ReportKPITargetCreateDto.builder()
            .targetId("T1")
            .hotelGroupCode(10L)
            .kpiCode("K1")
            .periodType("DAY")
            .periodValue("2024-01-01")
            .targetValue(new BigDecimal("100"))
            .build();

        when(kpiRepo.existsById("K1")).thenReturn(true);

        assertThatThrownBy(() -> service.create(dto))
            .isInstanceOf(com.gaekdam.gaekdambe.global.exception.CustomException.class);
    }

    @Test
    void get_notFound_throws() {
        when(targetRepo.findById(any(ReportKPITargetId.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get("T1", 5L))
            .isInstanceOf(com.gaekdam.gaekdambe.global.exception.CustomException.class);
    }

    @Test
    void list_filtersAndMaps() {
        ReportKPITargetId id1 = new ReportKPITargetId("A", 1L);
        ReportKPITarget t1 = new ReportKPITarget(); t1.setId(id1); t1.setKpiCode("K1");
        ReportKPITargetId id2 = new ReportKPITargetId("B", 1L);
        ReportKPITarget t2 = new ReportKPITarget(); t2.setId(id2); t2.setKpiCode("K2");

        when(targetRepo.findByIdHotelGroupCode(1L)).thenReturn(List.of(t1,t2));
        when(targetRepo.findByIdHotelGroupCodeAndKpiCode(1L, "K1")).thenReturn(List.of(t1));

        var all = service.list(1L, null);
        assertThat(all).hasSize(2);
        var filtered = service.list(1L, "K1");
        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0).getKpiCode()).isEqualTo("K1");
    }

    @Test
    void update_nonexistent_throws() {
        when(targetRepo.findById(any(ReportKPITargetId.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update("X", 1L, new ReportKPITargetUpdateDto()))
            .isInstanceOf(com.gaekdam.gaekdambe.global.exception.CustomException.class);
    }
}
