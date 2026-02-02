package com.gaekdam.gaekdambe.unit.analytics_service.report.dataset.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPITarget;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.query.repository.ReportKpiTargetRepository;

class MetricQueryServiceImplAdditionalTest {

    @Mock
    JdbcTemplate jdbc;

    @Mock
    ReportKpiTargetRepository targetRepo;

    private com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.MetricQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.MetricQueryServiceImpl(jdbc, targetRepo);
    }

    @Test
    void queryMetric_targetsFromJdbc_whenHotelGroupProvided() {
        // given
        when(jdbc.queryForObject(anyString(), eq(BigDecimal.class), any(), any(), any())).thenThrow(new EmptyResultDataAccessException(1));
        ReportKPITarget t = new ReportKPITarget();
        t.setTargetValue(BigDecimal.valueOf(100));
        when(targetRepo.findFirstByKpiCodeAndPeriodValue(anyString(), anyString())).thenReturn(Optional.of(t));

        // when
        com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.MetricResult r = service.queryMetric("checkin", "2025", Map.of("hotelGroupCode", "G1"));

        // then
        assertThat(r).isNotNull();
        assertThat(r.getTarget()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void computeRangeFromPeriod_invalid_throwsAndHandledByCaller() {
        // invalid period will be handled in method to fallback; call with bad period and assert not throwing
        com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.MetricResult r = service.queryMetric("checkin", "bad", Map.of());
        assertThat(r).isNotNull();
    }

    @Test
    void queryMetric_repeat_rate_zeroDivisionHandled() {
        // simulate zero total -> should return 0
        when(jdbc.queryForObject(anyString(), eq(BigDecimal.class), any(), any())).thenReturn(BigDecimal.ZERO);
        when(targetRepo.findFirstByKpiCodeAndPeriodValue(anyString(), anyString())).thenReturn(Optional.empty());
        when(targetRepo.findFirstByKpiCodeOrderByCreatedAtDesc(anyString())).thenReturn(Optional.empty());

        com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.MetricResult r = service.queryMetric("repeat_rate", "2025", Map.of());
        assertThat(r).isNotNull();
        assertThat(r.getActual()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
