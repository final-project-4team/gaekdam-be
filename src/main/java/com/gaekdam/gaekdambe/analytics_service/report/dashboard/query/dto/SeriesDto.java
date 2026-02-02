package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SeriesDto
 * - 시계열 데이터의 한 개 시리즈를 표현하는 DTO입니다.
 * - 기존에는 `name` 필드만 사용했으나, 프론트에서 `label`명을 기대하는 경우가 있어
 *   호환을 위해 `label` 필드를 추가합니다. (기존 코드 호환 유지)
 * - data: BigDecimal 권장 (숫자 또는 null)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeriesDto {
    // 기존 필드(하위 호환용)
    private String name;

    // 프론트에서 기대하는 필드명(alias)
    private String label;

    // 실제 값 배열
    private List<BigDecimal> data;

    public SeriesDto(String label, List<BigDecimal> data) {
        this.label = label;
        this.name = label; // 기존 코드를 위해 name도 동일값으로 설정
        this.data = data;
    }
}
