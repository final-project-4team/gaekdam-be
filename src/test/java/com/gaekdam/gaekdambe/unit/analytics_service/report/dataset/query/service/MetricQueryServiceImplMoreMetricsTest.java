package com.gaekdam.gaekdambe.unit.analytics_service.report.dataset.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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

class MetricQueryServiceImplMoreMetricsTest {

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
        // return total then member so computed percentage = 12/100*100 => 12
        when(jdbc.queryForObject(anyString(), eq(BigDecimal.class), any(), any())).thenReturn(BigDecimal.valueOf(100), BigDecimal.valueOf(12));
        com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.MetricResult r = service.queryMetric("membership_rate", "2025", Map.of());
        assertThat(r).isNotNull();
        assertThat(r.getActual()).isEqualByComparingTo(BigDecimal.valueOf(12));
    }

    @Test
    void queryMetric_foreignRate_nonNull() {
        // return foreignCount then totalCount so computed percentage = 3/100*100 => 3
        when(jdbc.queryForObject(anyString(), eq(BigDecimal.class), any(), any())).thenReturn(BigDecimal.valueOf(3), BigDecimal.valueOf(100));
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
