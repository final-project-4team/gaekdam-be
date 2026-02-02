package com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service;

import java.util.Map;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.ChartWidgetDto;

public interface MetricQueryService {
    // period 예: "2025" 또는 "2025-01" (widget.defaultPeriod에 따라)
    MetricResult queryMetric(String metricKey, String period, Map<String,Object> filter);

    // 시계열(기간별) 데이터를 조회하는 메서드
    // - 반환 형태: MetricTimeSeries (labels + series)
    // - 프론트에서 차트 렌더링에 사용되므로 라벨과 숫자 배열을 포함해야 함
    MetricTimeSeries queryMetricTimeSeries(String metricKey, String period, Map<String,Object> filter);

    // 고객유형 분포(개인/법인 등) - 차트 위젯 DTO 반환
    ChartWidgetDto queryCustomerContractDistribution(Map<String,Object> filter);

    // 외국인 TOP3 국가(Bar 차트) - 차트 위젯 DTO 반환
    ChartWidgetDto queryForeignTop3(Map<String,Object> filter);
}
