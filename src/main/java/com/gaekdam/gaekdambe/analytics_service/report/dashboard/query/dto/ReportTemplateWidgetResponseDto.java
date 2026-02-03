package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
    /* --- widget_Key ----
    CHECKIN_COUNT
    CHECKOUT_COUNT
    ADR
    OCCUPANCY_RATE
    GUEST_COUNT
    REPEAT_CUSTOMER_RATE
    MEMBERSHIP_RATE
    FOREIGN_CUSTOMER_RATE
    TOTAL_INQUIRY_COUNT
    CLAIM_COUNT
    UNRESOLVED_RATE
    AVG_RESPONSE_TIME
    RESERVATION_COUNT
    CANCEL_RATE
    NO_SHOW_RATE
    NON_ROOM_REVENUE_RATIO
    CHECKIN_COUNT
    CHECKOUT_COUNT
    ADR
    OCCUPANCY_RATE
    CHECKIN_COUNT
    CHECKOUT_COUNT
    ADR
    OCCUPANCY_RATE
    GUEST_COUNT
    REPEAT_CUSTOMER_RATE
    MEMBERSHIP_RATE
    FOREIGN_CUSTOMER_RATE
    CUSTOMER_TYPE_RATIO
    FOREIGN_TOP_COUNTRY
    TOTAL_INQUIRY_COUNT
    CLAIM_COUNT
    UNRESOLVED_RATE
    AVG_RESPONSE_TIME
    TOTAL_INQUIRY_COUNT
    CLAIM_COUNT
    RESERVATION_COUNT
    CANCEL_RATE
    NO_SHOW_RATE
    FACILITY_REVENUE_RATIO
    RESERVATION_COUNT
    CANCEL_COUNT
    NO_SHOW_COUNT
    FACILITY_REVENUE */
    private String widgetKey;       // metricKey (ex: "checkin")
    private String title;           // 표시명
    private String value;           // 포맷된 실제값 ("123" / "182,000원")
    // rawValue: 계산/CSV/다운로드용 원시 숫자값 (nullable)
    private BigDecimal rawValue;
    private String targetValue;     // 목표값(문자열)
    // 목표값의 원시 숫자 형태 (nullable)
    private BigDecimal targetValueRaw;
    private Double changePct;       // 목표 대비 증감율 (예: -4.2 or 23.0)
    private String trend;           // "up" / "down" / "neutral"
    private Integer sortOrder;

    // --- 아래 필드들은 시계열(차트) 렌더링을 위한 확장 필드입니다 ---
    // widgetType: "KPI_CARD", "LINE", "GAUGE", "BAR" 
    // - 기존 필드들과 호환성을 유지하기 위해 null 가능(optional)하게 설계했습니다.
    private String widgetType;

    // labels: x축 라벨 목록 (예: ["2023-01", "2023-02", ...] 또는 ["1월","2월",...])
    private List<String> labels;

    // series: 시계열 값 리스트. 각 SeriesDto는 이름(name)과 숫자 배열(data)을 가집니다.
    // 프론트에서 series.data 값이 숫자 또는 null이면 Chart.js에서 제대로 핸들링합니다.
    private List<SeriesDto> series;

    // meta: 추가 메타정보를 담는 맵. (chartKind, valueType, topN 등)
    private Map<String, Object> meta;

    // SeriesDto는 같은 패키지에 별도 파일로 정의됩니다.
}
