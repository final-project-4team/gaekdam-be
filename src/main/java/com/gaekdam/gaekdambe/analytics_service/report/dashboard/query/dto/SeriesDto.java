package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SeriesDto
 * - 시계열 데이터의 한 개 시리즈를 표현하는 DTO입니다.
 * - name: 시리즈명 (예: "actual" / "target")
 * - data: 시계열 값 목록 (BigDecimal 권장, 프론트는 숫자 또는 null로 처리)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeriesDto {
    private String name;
    private List<BigDecimal> data;
}
