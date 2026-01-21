package com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service;

import java.util.Map;

public interface MetricQueryService {
    // period 예: "2025" 또는 "2025-01" (widget.defaultPeriod에 따라)
    MetricResult queryMetric(String metricKey, String period, Map<String,Object> filter);
}
