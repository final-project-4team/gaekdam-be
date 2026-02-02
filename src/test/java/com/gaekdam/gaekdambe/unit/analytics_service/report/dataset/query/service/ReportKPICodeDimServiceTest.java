package com.gaekdam.gaekdambe.unit.analytics_service.report.dataset.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPICodeDim;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.infrastructure.repository.ReportKPICodeDimRepository;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.query.dto.KpiCodeDto;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.ReportKPICodeDimService;

import java.util.Arrays;

class ReportKPICodeDimServiceTest {

    @Mock
    ReportKPICodeDimRepository repo;

    private ReportKPICodeDimService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ReportKPICodeDimService(repo);
    }

    @Test
    void listActive_returnsMappedDtos() {
        ReportKPICodeDim e1 = new ReportKPICodeDim();
        e1.setKpiCode("K1");
        e1.setKpiName("Name1");
        e1.setUnit("unit1");
        e1.setDescription("desc1");

        ReportKPICodeDim e2 = new ReportKPICodeDim();
        e2.setKpiCode("K2");
        e2.setKpiName("Name2");
        e2.setUnit("unit2");
        e2.setDescription("desc2");

        when(repo.findByIsActiveTrueOrderByKpiCodeAsc()).thenReturn(Arrays.asList(e1, e2));

        List<KpiCodeDto> dtos = service.listActive();

        assertThat(dtos).isNotNull();
        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getKpiCode()).isEqualTo("K1");
        assertThat(dtos.get(1).getKpiCode()).isEqualTo("K2");
    }
}
