package com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPITarget;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.query.repository.ReportKpiTargetRepository;

class MetricQueryServiceImplTest {

    @Mock
    JdbcTemplate jdbc;

    @Mock
    ReportKpiTargetRepository targetRepo;

    private MetricQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new MetricQueryServiceImpl(jdbc, targetRepo);
    }

    @Test
    void queryMetric_invalidPeriod_defaultsToCurrentMonth() {
        // given
        when(jdbc.queryForObject(anyString(), eq(BigDecimal.class), any())).thenReturn(BigDecimal.valueOf(10));
        when(targetRepo.findFirstByKpiCodeAndPeriodValue(anyString(), anyString())).thenReturn(Optional.of(new ReportKPITarget()));

        // when
        MetricResult r = service.queryMetric("checkin", "bad-format", Map.of());

        // then
        assertThat(r).isNotNull();
        assertThat(r.getActual()).isNotNull();
    }

    @Test
    void queryMetric_checkin_callsJdbcAndReturnsBigDecimal() {
        // given
        // stub the overload used by MetricQueryServiceImpl: queryForObject(sql, Integer.class, Timestamp, Timestamp)
        when(jdbc.queryForObject(anyString(), eq(Integer.class), any(java.sql.Timestamp.class), any(java.sql.Timestamp.class))).thenReturn(5);
        when(targetRepo.findFirstByKpiCodeAndPeriodValue(anyString(), anyString())).thenReturn(Optional.empty());
        when(targetRepo.findFirstByKpiCodeOrderByCreatedAtDesc(anyString())).thenReturn(Optional.empty());

        // when
        MetricResult r = service.queryMetric("checkin", "2025-01", Map.of());

        // then
        assertThat(r).isNotNull();
        assertThat(r.getActual()).isEqualByComparingTo(BigDecimal.valueOf(5));
        assertThat(r.getFormattedActual()).isNotNull();
    }

    @Test
    void queryMetric_adr_withZeroOccupiedNights_returnsZero() {
        // given
        when(jdbc.queryForObject(anyString(), eq(java.math.BigDecimal.class), any(), any(), any(), any())).thenReturn(java.math.BigDecimal.ZERO);
        when(jdbc.queryForObject(anyString(), eq(Integer.class), any(), any(), any(), any())).thenReturn(0);
        when(targetRepo.findFirstByKpiCodeAndPeriodValue(anyString(), anyString())).thenReturn(Optional.empty());
        when(targetRepo.findFirstByKpiCodeOrderByCreatedAtDesc(anyString())).thenReturn(Optional.empty());

        // when
        MetricResult r = service.queryMetric("adr", "2025-01", Map.of());

        // then
        assertThat(r).isNotNull();
        assertThat(r.getActual()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(r.getFormattedActual()).isNotNull();
    }
}
