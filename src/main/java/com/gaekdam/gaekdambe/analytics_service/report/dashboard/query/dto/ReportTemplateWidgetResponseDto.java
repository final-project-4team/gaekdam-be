package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportTemplateWidgetResponseDto {
    private Long templateWidgetId;
    private Long templateId;
    private String widgetKey;       // metricKey (ex: "checkin")
    private String title;           // 표시명
    private String value;           // 포맷된 실제값 ("123" / "182,000원")
    private String targetValue;     // 목표값(문자열)
    private Double changePct;       // 목표 대비 증감율 (예: -4.2 or 23.0)
    private String trend;           // "up" / "down" / "neutral"
    private Integer sortOrder;
    // getters / setters / 생성자
}
