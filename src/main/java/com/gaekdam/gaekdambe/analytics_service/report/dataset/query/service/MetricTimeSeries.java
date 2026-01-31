package com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MetricTimeSeries
 * - 시계열 데이터(차트용)를 표현하는 단순 DTO
 * - labels: x축 라벨 목록
 * - series: 데이터 시리즈 목록 (name + data[])
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricTimeSeries {
    private List<String> labels;
    private List<Series> series;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Series {
        private String name;
        private List<BigDecimal> data;
    }
}
