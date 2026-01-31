package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportTemplateWidget;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.infrastructure.repository.ReportTemplateRepository;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.ReportTemplateWidgetResponseDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.SeriesDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.repository.ReportTemplateWidgetQueryRepository;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.MetricQueryService;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.MetricResult;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.MetricTimeSeries;
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
    // 우선 DB에 저장된 widgetType을 우선 사용합니다. (예: "KPI_CARD","LINE","GAUGE","BAR")
    // 이전 버전과의 호환성을 위해 제목 기반 판별은 fallback으로 유지합니다.
    boolean isTimeSeries = false;
    String widgetTypeFromEntity = w.getWidgetType();
    if (widgetTypeFromEntity != null && widgetTypeFromEntity.equalsIgnoreCase("LINE")) {
      isTimeSeries = true;
    }

    // fallback: 제목에 '변화' 또는 '변화량'이 포함된 경우 시계열로 간주
    if (!isTimeSeries) {
      String titleLower = w.getTitle() == null ? "" : w.getTitle().toLowerCase();
      if (titleLower.contains("변화") || titleLower.contains("변화량") || titleLower.contains("change")) {
        isTimeSeries = true;
      }
    }

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
    // DTO 빌더 시작
    ReportTemplateWidgetResponseDto.ReportTemplateWidgetResponseDtoBuilder b = ReportTemplateWidgetResponseDto.builder()
        .templateWidgetId(w.getTemplateWidgetId())
        .templateId(w.getTemplateId())
        .widgetKey(w.getMetricKey()) // 엔티티의 metricKey를 DTO widgetKey로 매핑
        .title(w.getTitle())
        .value(value)
        .targetValue(targetValue)
        .changePct(changePct)
        .trend(trend)
        .sortOrder(w.getDefaultSortOrder());

    // 시계열 위젯인 경우 MetricQueryService에서 시계열 데이터를 조회하여 DTO에 주입
    if (isTimeSeries) {
      try {
        MetricTimeSeries mts = metricService.queryMetricTimeSeries(w.getMetricKey(), period, Map.of());
        if (mts != null) {
          // entity에 저장된 widgetType을 우선 전달. 없으면 기본값 "LINE"을 사용
          b.widgetType(widgetTypeFromEntity != null ? widgetTypeFromEntity : "LINE");
          b.labels(mts.getLabels());
          if (mts.getSeries() != null) {
            // MetricTimeSeries.Series -> SeriesDto 매핑
            java.util.List<SeriesDto> seriesDtos = mts.getSeries().stream()
                .map(s -> new SeriesDto(s.getName(), s.getData()))
                .toList();
            b.series(seriesDtos);
          }
        } else {
          // service가 null을 반환하면 안전하게 widgetType만 표기 (프론트는 빈 series/labels로 처리)
          b.widgetType(widgetTypeFromEntity != null ? widgetTypeFromEntity : "LINE");
        }
      } catch (Exception ex) {
        // 시계열 조회 실패 시 로깅하고 기본 KPI 필드만 내려줍니다. (실 서비스에서는 에러 원인 상세 기록)
        System.err.println("Failed to load timeseries for widget: " + w.getTemplateWidgetId() + ", " + ex.getMessage());
        b.widgetType(widgetTypeFromEntity != null ? widgetTypeFromEntity : "LINE");
      }
    } else {
      // KPI 카드 타입 명시적: entity에 있는 값을 우선 사용
      b.widgetType(widgetTypeFromEntity != null ? widgetTypeFromEntity : "KPI_CARD");
    }

    return b.build();
  }
}
