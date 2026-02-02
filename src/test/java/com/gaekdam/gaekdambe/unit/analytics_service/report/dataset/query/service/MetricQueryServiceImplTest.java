package com.gaekdam.gaekdambe.unit.analytics_service.report.dataset.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPITarget;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.query.repository.ReportKpiTargetRepository;

class MetricQueryServiceImplTest {

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
    void queryMetric_invalidPeriod_defaultsToCurrentMonth() {
        // given
        when(jdbc.queryForObject(anyString(), eq(BigDecimal.class), any())).thenReturn(BigDecimal.valueOf(10));
        when(targetRepo.findFirstByKpiCodeAndPeriodValue(anyString(), anyString())).thenReturn(Optional.of(new ReportKPITarget()));

        // when
        com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.MetricResult r = service.queryMetric("checkin", "bad-format", Map.of());

        // then
        assertThat(r).isNotNull();
        assertThat(r.getActual()).isNotNull();
    }

    @Test
    void queryMetric_checkin_callsJdbcAndReturnsBigDecimal() {
        // given
        // match two timestamp args used by the service implementation
        when(jdbc.queryForObject(anyString(), eq(Integer.class), any(), any())).thenReturn(5);
        when(targetRepo.findFirstByKpiCodeAndPeriodValue(anyString(), anyString())).thenReturn(Optional.empty());
        when(targetRepo.findFirstByKpiCodeOrderByCreatedAtDesc(anyString())).thenReturn(Optional.empty());

        // when
        com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.MetricResult r = service.queryMetric("checkin", "2025-01", Map.of());

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
        com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.MetricResult r = service.queryMetric("adr", "2025-01", Map.of());

        // then
        assertThat(r).isNotNull();
        assertThat(r.getActual()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(r.getFormattedActual()).isNotNull();
    }

    // additional tests merged from MetricQueryServiceImplAdditionalTest
    @Test
    void queryMetric_targetsFromJdbc_whenHotelGroupProvided() {
        // simulate jdbc no-result so repo fallback is used
        when(jdbc.queryForObject(anyString(), eq(BigDecimal.class), any(), any(), any())).thenThrow(new EmptyResultDataAccessException(1));
        ReportKPITarget t = new ReportKPITarget();
        t.setTargetValue(BigDecimal.valueOf(100));
        when(targetRepo.findFirstByKpiCodeAndPeriodValue(anyString(), anyString())).thenReturn(Optional.of(t));

        com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.MetricResult r = service.queryMetric("checkin", "2025", Map.of("hotelGroupCode", "G1"));

        assertThat(r).isNotNull();
        assertThat(r.getTarget()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void computeRangeFromPeriod_invalid_throwsAndHandledByCaller() {
        com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.MetricResult r = service.queryMetric("checkin", "bad", Map.of());
        assertThat(r).isNotNull();
    }

    @Test
    void queryMetric_repeat_rate_zeroDivisionHandled() {
        when(jdbc.queryForObject(anyString(), eq(BigDecimal.class), any(), any())).thenReturn(BigDecimal.ZERO);
        when(targetRepo.findFirstByKpiCodeAndPeriodValue(anyString(), anyString())).thenReturn(Optional.empty());
        when(targetRepo.findFirstByKpiCodeOrderByCreatedAtDesc(anyString())).thenReturn(Optional.empty());

        com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.MetricResult r = service.queryMetric("repeat_rate", "2025", Map.of());
        assertThat(r).isNotNull();
        assertThat(r.getActual()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void queryMetric_checkout_returnsNonNullAndUsesTargetWhenJdbcEmpty() {
        // jdbc throws -> fallback to target repo
        when(jdbc.queryForObject(anyString(), eq(BigDecimal.class), any(), any(), any())).thenThrow(new EmptyResultDataAccessException(1));
        ReportKPITarget t = new ReportKPITarget();
        t.setTargetValue(BigDecimal.valueOf(77));
        when(targetRepo.findFirstByKpiCodeAndPeriodValue(anyString(), anyString())).thenReturn(Optional.of(t));

        com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.MetricResult r = service.queryMetric("checkout", "2025", Map.of());
        assertThat(r).isNotNull();
        assertThat(r.getTarget()).isEqualByComparingTo(BigDecimal.valueOf(77));
    }

    @Test
    void queryMetric_occRate_zeroHandled() {
        when(jdbc.queryForObject(anyString(), eq(BigDecimal.class), any(), any())).thenReturn(BigDecimal.ZERO);
        when(targetRepo.findFirstByKpiCodeAndPeriodValue(anyString(), anyString())).thenReturn(Optional.empty());
        when(targetRepo.findFirstByKpiCodeOrderByCreatedAtDesc(anyString())).thenReturn(Optional.empty());

        com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.MetricResult r = service.queryMetric("occ_rate", "2025", Map.of());
        assertThat(r).isNotNull();
        assertThat(r.getActual()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void queryMetric_membershipRate_nonNull() {
        when(jdbc.queryForObject(anyString(), eq(BigDecimal.class), any(), any())).thenReturn(BigDecimal.valueOf(12));
        com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.MetricResult r = service.queryMetric("membership_rate", "2025", Map.of());
        assertThat(r).isNotNull();
        assertThat(r.getActual()).isEqualByComparingTo(BigDecimal.valueOf(12));
    }

    @Test
    void queryMetric_foreignRate_nonNull() {
        when(jdbc.queryForObject(anyString(), eq(BigDecimal.class), any(), any())).thenReturn(BigDecimal.valueOf(3));
        com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.MetricResult r = service.queryMetric("foreign_rate", "2025", Map.of());
        assertThat(r).isNotNull();
        assertThat(r.getActual()).isEqualByComparingTo(BigDecimal.valueOf(3));
    }

    @Test
    void queryMetric_inquiryAndClaim_countsReturnValues() {
        when(jdbc.queryForObject(anyString(), eq(BigDecimal.class), any(), any())).thenReturn(BigDecimal.valueOf(5));
        com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.MetricResult r1 = service.queryMetric("inquiry_count", "2025", Map.of());
        com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.MetricResult r2 = service.queryMetric("claim_count", "2025", Map.of());
        assertThat(r1).isNotNull();
        assertThat(r1.getActual()).isEqualByComparingTo(BigDecimal.valueOf(5));
        assertThat(r2).isNotNull();
        assertThat(r2.getActual()).isEqualByComparingTo(BigDecimal.valueOf(5));
    }
}
