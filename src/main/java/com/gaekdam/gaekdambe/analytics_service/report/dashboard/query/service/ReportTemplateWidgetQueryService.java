package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportTemplateWidget;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.infrastructure.repository.ReportTemplateRepository;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.ChartWidgetDto;
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

    // --- 신규: widgetKey(=metricKey)를 기준으로 차트 위젯 생성 호출 처리 ---
    try {
      // 우선적으로 metricKey(=widgetKey) 기반 매핑을 사용합니다.
      // 이유: DB에 저장된 widgetType이 일관되지 않을 수 있으므로, widget의 실제 역할(widgetKey)에 따라 적절한 차트 서비스를 호출해야 합니다.
      String widgetKey = w.getMetricKey() == null ? "" : w.getMetricKey().toUpperCase();
      // period(템플릿 호출에서 전달된 파라미터)를 필터에 포함합니다. metricService는 filter 내의 "period" 키를 참조하여 기간 필터링을 수행합니다.
      java.util.Map<String,Object> filter = new java.util.HashMap<>();
      if (w.getHotelGroupCode() != null) filter.put("hotelGroupCode", w.getHotelGroupCode());
      if (period != null) filter.put("period", period);

      if ("CUSTOMER_TYPE_RATIO".equals(widgetKey)) {
        // 고객유형 분포 (프론트는 GAUGE 타입으로 기대)
        ChartWidgetDto chart = metricService.queryCustomerContractDistribution(filter);
        if (chart != null) {
          b.widgetType(widgetTypeFromEntity != null ? widgetTypeFromEntity : "GAUGE");
          b.labels(chart.getLabels());
          if (chart.getSeries() != null && !chart.getSeries().isEmpty()) {
            java.util.List<SeriesDto> s = chart.getSeries().stream()
                .map(cs -> new SeriesDto(cs.getName(), cs.getData()))
                .toList();
            b.series(s);
          } else {
            b.series(java.util.Collections.emptyList());
          }
          b.meta(chart.getMeta());
        } else {
          // 안전하게 빈 차트 구조를 내려주어 프론트가 null 체크 없이 렌더링 가능하도록 함
          b.widgetType(widgetTypeFromEntity != null ? widgetTypeFromEntity : "GAUGE");
          b.labels(java.util.Collections.emptyList());
          b.series(java.util.Collections.emptyList());
          b.meta(java.util.Collections.emptyMap());
        }

      } else if ("FOREIGN_TOP_COUNTRY".equals(widgetKey) || "FOREIGN_TOP3".equals(widgetKey) || "FOREIGN_TOP_COUNTRIES".equals(widgetKey)) {
        // 외국인 Top N (프론트는 BAR 타입으로 기대)
        ChartWidgetDto chart = metricService.queryForeignTop3(filter);
        if (chart != null) {
          b.widgetType(widgetTypeFromEntity != null ? widgetTypeFromEntity : "BAR");
          b.labels(chart.getLabels());
          if (chart.getSeries() != null && !chart.getSeries().isEmpty()) {
            java.util.List<SeriesDto> s = chart.getSeries().stream()
                .map(cs -> new SeriesDto(cs.getName(), cs.getData()))
                .toList();
            b.series(s);
          } else {
            b.series(java.util.Collections.emptyList());
          }
          b.meta(chart.getMeta());
        } else {
          b.widgetType(widgetTypeFromEntity != null ? widgetTypeFromEntity : "BAR");
          b.labels(java.util.Collections.emptyList());
          b.series(java.util.Collections.emptyList());
          b.meta(java.util.Collections.emptyMap());
        }

      } else if (isTimeSeries) {
        // 기존 시계열 처리 로직을 유지합니다.
        try {
          MetricTimeSeries mts = metricService.queryMetricTimeSeries(w.getMetricKey(), period, Map.of());
          if (mts != null) {
            b.widgetType(widgetTypeFromEntity != null ? widgetTypeFromEntity : "LINE");
            b.labels(mts.getLabels());
            if (mts.getSeries() != null) {
              java.util.List<SeriesDto> seriesDtos = mts.getSeries().stream()
                  .map(s -> new SeriesDto(s.getName(), s.getData()))
                  .toList();
              b.series(seriesDtos);
            } else {
              b.series(java.util.Collections.emptyList());
            }
          } else {
            b.widgetType(widgetTypeFromEntity != null ? widgetTypeFromEntity : "LINE");
            b.labels(java.util.Collections.emptyList());
            b.series(java.util.Collections.emptyList());
            b.meta(java.util.Collections.emptyMap());
          }
        } catch (Exception ex) {
          System.err.println("Failed to load timeseries for widget: " + w.getTemplateWidgetId() + ", " + ex.getMessage());
          b.widgetType(widgetTypeFromEntity != null ? widgetTypeFromEntity : "LINE");
          b.labels(java.util.Collections.emptyList());
          b.series(java.util.Collections.emptyList());
          b.meta(java.util.Collections.emptyMap());
        }
      } else {
        // KPI 카드 또는 처리할 차트 없음: entity에 있는 값을 우선 사용하고 차트 필드는 빈값으로 채웁니다.
        b.widgetType(widgetTypeFromEntity != null ? widgetTypeFromEntity : "KPI_CARD");
        b.labels(java.util.Collections.emptyList());
        b.series(java.util.Collections.emptyList());
        b.meta(java.util.Collections.emptyMap());
      }
    } catch (Exception ex) {
      // 위젯별 차트 생성 중 예외 발생 시, 기본 KPI 카드 정보를 내려주고 오류 로그를 남깁니다.
      System.err.println("Failed to build chart widget for templateWidgetId=" + w.getTemplateWidgetId() + ", " + ex.getMessage());
      b.widgetType(widgetTypeFromEntity != null ? widgetTypeFromEntity : "KPI_CARD");
      b.labels(java.util.Collections.emptyList());
      b.series(java.util.Collections.emptyList());
      b.meta(java.util.Collections.emptyMap());
    }

    return b.build();
  }
}
