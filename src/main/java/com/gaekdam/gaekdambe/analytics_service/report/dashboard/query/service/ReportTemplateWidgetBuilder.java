package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.ReportTemplateWidgetResponseDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.SeriesDto;

/**
 * ReportTemplateWidgetBuilder
 * - 임시로 템플릿 위젯 응답 DTO를 조립하는 유틸 컴포넌트 예시입니다.
 * - 실제 서비스에서는 Repository/QueryService에서 집계 데이터를 받아 이 빌더를 사용해 DTO를 생성합니다.
 */
@Component
public class ReportTemplateWidgetBuilder {

    // 고객유형 비율 더미 빌더
    public ReportTemplateWidgetResponseDto buildCustomerTypeWidget(long templateWidgetId) {
        ReportTemplateWidgetResponseDto dto = ReportTemplateWidgetResponseDto.builder()
            .templateWidgetId(templateWidgetId)
            .templateId(3L)
            .widgetKey("CUSTOMER_TYPE_RATIO")
            .title("고객유형 비율")
            .value("0")
            .sortOrder(5)
            .widgetType("DOUGHNUT")
            .build();

        List<String> labels = List.of("개인","단체","법인");
        List<BigDecimal> vals = List.of(BigDecimal.valueOf(570), BigDecimal.valueOf(270), BigDecimal.valueOf(160));
        SeriesDto s = new SeriesDto("건수", vals);
        dto.setLabels(labels);
        dto.setSeries(List.of(s));
        Map<String,Object> meta = new HashMap<>(); meta.put("valueType","count"); meta.put("chartKind","doughnut"); dto.setMeta(meta);
        return dto;
    }

    // 외국인 TOP3 빌더
    public ReportTemplateWidgetResponseDto buildForeignTop3Widget(long templateWidgetId) {
        ReportTemplateWidgetResponseDto dto = ReportTemplateWidgetResponseDto.builder()
            .templateWidgetId(templateWidgetId)
            .templateId(3L)
            .widgetKey("FOREIGN_TOP_COUNTRY")
            .title("외국인 고객 Top3 국가")
            .value("0")
            .sortOrder(6)
            .widgetType("BAR")
            .build();

        List<String> labels = List.of("중국","일본","대만");
        List<BigDecimal> vals = List.of(BigDecimal.valueOf(340), BigDecimal.valueOf(200), BigDecimal.valueOf(70));
        SeriesDto s = new SeriesDto("건수", vals);
        dto.setLabels(labels);
        dto.setSeries(List.of(s));
        Map<String,Object> meta = new HashMap<>(); meta.put("valueType","count"); meta.put("chartKind","bar"); meta.put("topN",3); dto.setMeta(meta);
        return dto;
    }
}
