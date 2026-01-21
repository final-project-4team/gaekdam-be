package com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class MetricResult {
    private BigDecimal actual;
    private BigDecimal target;
    private String formattedActual; // 선택적: 이미 포맷된 문자열
    private String formattedTarget;
    private Double changePct;
    private String trend;
}
