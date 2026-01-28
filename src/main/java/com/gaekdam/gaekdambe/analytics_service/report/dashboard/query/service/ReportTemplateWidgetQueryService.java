package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportTemplateWidget;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.infrastructure.repository.ReportTemplateRepository;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.ReportTemplateWidgetResponseDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.repository.ReportTemplateWidgetQueryRepository;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.MetricQueryService;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.MetricResult;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportTemplateWidgetQueryService {

  private final ReportTemplateRepository templateRepo;
  private final ReportTemplateWidgetQueryRepository widgetRepo;
  private final MetricQueryService metricService; // 새로 주입

  // 기존 호출을 유지하면서 period를 명시적으로 받을 수 있도록 오버로드
  public List<ReportTemplateWidgetResponseDto> listByTemplateId(Long templateId) {
    return listByTemplateId(templateId, null);
  }

  public List<ReportTemplateWidgetResponseDto> listByTemplateId(Long templateId, String period) {

    // 템플릿 존재 검증 (권장)
    if (!templateRepo.existsById(templateId)) {
      throw new CustomException(ErrorCode.REPORT_TEMPLATE_NOT_FOUND);
    }

    return widgetRepo.findByTemplateIdOrderByDefaultSortOrderAsc(templateId)
        .stream()
        .map(w -> toDto(w, period))
        .toList();
  }

  private ReportTemplateWidgetResponseDto toDto(ReportTemplateWidget w, String period) {
    // metricService로 실제값/목표값/증감율 계산
    MetricResult mr = metricService.queryMetric(w.getMetricKey(), period, Map.of());

    String value = null;
    String targetValue = null;
    Double changePct = null;
    String trend = null;

    if (mr != null) {
      value = mr.getFormattedActual() != null ? mr.getFormattedActual()
          : (mr.getActual() != null ? mr.getActual().toPlainString() : "0");
      targetValue = mr.getFormattedTarget() != null ? mr.getFormattedTarget()
          : (mr.getTarget() != null ? mr.getTarget().toPlainString() : null);
      changePct = mr.getChangePct();
      trend = mr.getTrend();
    }

    return ReportTemplateWidgetResponseDto.builder()
        .templateWidgetId(w.getTemplateWidgetId())
        .templateId(w.getTemplateId())
        .widgetKey(w.getMetricKey()) // 엔티티의 metricKey를 DTO widgetKey로 매핑
        .title(w.getTitle())
        .value(value)
        .targetValue(targetValue)
        .changePct(changePct)
        .trend(trend)
        .sortOrder(w.getDefaultSortOrder())
        .build();
  }
}
