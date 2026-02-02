package com.gaekdam.gaekdambe.unit.analytics_service.report.dashboard.query.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportTemplateWidget;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.infrastructure.repository.ReportTemplateRepository;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.ChartWidgetDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.ReportTemplateWidgetResponseDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.repository.ReportTemplateWidgetQueryRepository;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.MetricQueryService;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;

class ReportTemplateWidgetQueryServiceTest {

    ReportTemplateRepository templateRepo;
    ReportTemplateWidgetQueryRepository widgetRepo;
    MetricQueryService metricService;
    com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.service.ReportTemplateWidgetQueryService service;

    @BeforeEach
    void setUp() {
        templateRepo = mock(ReportTemplateRepository.class);
        widgetRepo = mock(ReportTemplateWidgetQueryRepository.class);
        metricService = mock(MetricQueryService.class);
        service = new com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.service.ReportTemplateWidgetQueryService(templateRepo, widgetRepo, metricService);
    }

    @Test
    void listByTemplateId_whenTemplateNotExists_throws() {
        when(templateRepo.existsById(1L)).thenReturn(false);

        CustomException ex = assertThrows(CustomException.class, () -> service.listByTemplateId(1L));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.REPORT_TEMPLATE_NOT_FOUND);
    }

    @Test
    void listByTemplateId_customerTypeRatio_buildsChart() {
        // given
        when(templateRepo.existsById(3L)).thenReturn(true);
        ReportTemplateWidget w = new ReportTemplateWidget();
        w.setTemplateWidgetId(55L);
        w.setTemplateId(3L);
        w.setMetricKey("CUSTOMER_TYPE_RATIO");
        w.setTitle("고객유형 비율");

        ChartWidgetDto chart = ChartWidgetDto.ofDonut(List.of("A","B"), List.of(BigDecimal.ONE, BigDecimal.TEN), Map.of("valueType","count","chartKind","doughnut"));
        when(widgetRepo.findByTemplateIdOrderByDefaultSortOrderAsc(3L)).thenReturn(List.of(w));
        when(metricService.queryCustomerContractDistribution(any())).thenReturn(chart);

        // when
        List<ReportTemplateWidgetResponseDto> res = service.listByTemplateId(3L);

        // then
        assertThat(res).hasSize(1);
        ReportTemplateWidgetResponseDto dto = res.get(0);
        assertThat(dto.getTemplateWidgetId()).isEqualTo(55L);
        assertThat(dto.getWidgetKey()).isEqualTo("CUSTOMER_TYPE_RATIO");
        assertThat(dto.getLabels()).containsExactly("A","B");
        verify(metricService).queryCustomerContractDistribution(any());
    }

    @Test
    void listByTemplateId_foreignTop_buildsChartWithEmptyWhenNull() {
        when(templateRepo.existsById(3L)).thenReturn(true);
        ReportTemplateWidget w = new ReportTemplateWidget();
        w.setTemplateWidgetId(56L);
        w.setTemplateId(3L);
        w.setMetricKey("FOREIGN_TOP_COUNTRY");
        w.setTitle("외국인 Top");

        when(widgetRepo.findByTemplateIdOrderByDefaultSortOrderAsc(3L)).thenReturn(List.of(w));
        when(metricService.queryForeignTop3(any())).thenReturn(null);

        List<ReportTemplateWidgetResponseDto> res = service.listByTemplateId(3L);
        assertThat(res).hasSize(1);
        ReportTemplateWidgetResponseDto dto = res.get(0);
        assertThat(dto.getLabels()).isEmpty();
        assertThat(dto.getSeries()).isEmpty();
    }

    @Test
    void listByTemplateId_timeseries_whenTitleIndicatesChange_usesTimeSeries() {
        when(templateRepo.existsById(5L)).thenReturn(true);
        ReportTemplateWidget w = new ReportTemplateWidget();
        w.setTemplateWidgetId(77L);
        w.setTemplateId(5L);
        w.setMetricKey("SOME_METRIC");
        w.setTitle("변화량 측정");

        com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.MetricTimeSeries mts = new com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.MetricTimeSeries(List.of("2025-01","2025-02"), List.of(new com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.MetricTimeSeries.Series("s1", List.of(BigDecimal.ONE, BigDecimal.TEN))));
        when(widgetRepo.findByTemplateIdOrderByDefaultSortOrderAsc(5L)).thenReturn(List.of(w));
        when(metricService.queryMetricTimeSeries(eq("SOME_METRIC"), any(), any())).thenReturn(mts);

        List<ReportTemplateWidgetResponseDto> res = service.listByTemplateId(5L);
        assertThat(res).hasSize(1);
        ReportTemplateWidgetResponseDto dto = res.get(0);
        assertThat(dto.getWidgetKey()).isEqualTo("SOME_METRIC");
        assertThat(dto.getLabels()).containsExactly("2025-01","2025-02");
        assertThat(dto.getSeries()).hasSize(1);
    }

}
