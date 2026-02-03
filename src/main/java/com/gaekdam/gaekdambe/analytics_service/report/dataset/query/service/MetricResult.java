package com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service;

import java.math.BigDecimal;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class MetricResult {
    @Schema(description = "실적 수치", example = "950.00")
    private BigDecimal actual;

    @Schema(description = "목표 수치", example = "1000.00")
    private BigDecimal target;

    @Schema(description = "포맷된 실적 수치", example = "950")
    private String formattedActual; // 선택적: 이미 포맷된 문자열

    @Schema(description = "포맷된 목표 수치", example = "1,000")
    private String formattedTarget;

    @Schema(description = "변동률 (%)", example = "-5.0")
    private Double changePct;

    @Schema(description = "트렌드 (UP/DOWN/STABLE)", example = "DOWN")
    private String trend;
}
