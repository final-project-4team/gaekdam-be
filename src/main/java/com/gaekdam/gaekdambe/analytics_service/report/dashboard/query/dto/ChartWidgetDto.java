package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 차트 위젯 응답 DTO
 *
 * 프론트엔드에서 차트(도넛, 바 등)를 렌더링하기 위해 사용하는 공통 포맷입니다.
 * - widgetType: 위젯 타입 문자열 (예: "donut", "bar")
 * - labels: 각 데이터 항목의 라벨(순서가 중요)
 * - series: 시리즈 목록(이 예제에서는 보통 하나의 시리즈 "actual" 사용)
 * - meta: 추가 메타 정보(총합, 색상맵 등 자유롭게 포함 가능)
 *
 * 예시 JSON 응답:
 * {
 *   "widgetType":"donut",
 *   "labels":["INDIVIDUAL","CORPORATE"],
 *   "series":[{"name":"actual","data":[123,45]}],
 *   "meta":{"total":168}
 * }
 */
public class ChartWidgetDto {
    // 차트 타입 (예: "donut", "bar") - 프론트에서 렌더러를 선택할 때 사용
    private String widgetType;

    // 라벨 배열: 차트 조각/막대의 라벨들. 프론트는 이 순서를 기준으로 데이터를 매칭함
    private List<String> labels;

    // 시리즈: 시계열 혹은 단일 시리즈 데이터. 보통 하나의 시리즈로 실제(actual) 값을 전달
    private List<Series> series;

    // 추가 메타 정보: 총합(total), 색상, 단위 등 프론트가 필요로 하는 임의의 키/값
    private Map<String,Object> meta;

    public ChartWidgetDto() {
        this.labels = new ArrayList<>();
        this.series = new ArrayList<>();
    }

    public ChartWidgetDto(String widgetType, List<String> labels, List<Series> series, Map<String,Object> meta) {
        this.widgetType = widgetType;
        this.labels = labels == null ? new ArrayList<>() : new ArrayList<>(labels);
        this.series = series == null ? new ArrayList<>() : new ArrayList<>(series);
        this.meta = meta;
    }

    public String getWidgetType() {
        return widgetType;
    }

    public void setWidgetType(String widgetType) {
        this.widgetType = widgetType;
    }

    public List<String> getLabels() {
        return labels == null ? Collections.emptyList() : Collections.unmodifiableList(labels);
    }

    public void setLabels(List<String> labels) {
        this.labels = labels == null ? new ArrayList<>() : new ArrayList<>(labels);
    }

    public List<Series> getSeries() {
        return series == null ? Collections.emptyList() : Collections.unmodifiableList(series);
    }

    public void setSeries(List<Series> series) {
        this.series = series == null ? new ArrayList<>() : new ArrayList<>(series);
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    // convenience factory for common donut chart with one series named "actual"
    public static ChartWidgetDto ofDonut(List<String> labels, List<BigDecimal> data, Map<String,Object> meta) {
        Series s = new Series("actual", data);
        List<Series> series = new ArrayList<>();
        series.add(s);
        return new ChartWidgetDto("donut", labels, series, meta);
    }

    public static class Series {
        private String name;
        private List<BigDecimal> data;

        public Series() {
            this.data = new ArrayList<>();
        }

        public Series(String name, List<BigDecimal> data) {
            this.name = name;
            this.data = data == null ? new ArrayList<>() : new ArrayList<>(data);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<BigDecimal> getData() {
            return data == null ? Collections.emptyList() : Collections.unmodifiableList(data);
        }

        public void setData(List<BigDecimal> data) {
            this.data = data == null ? new ArrayList<>() : new ArrayList<>(data);
        }

        @Override
        public String toString() {
            return "Series{name='" + name + "', data=" + data + '}';
        }
    }

    @Override
    public String toString() {
        return "ChartWidgetDto{" +
                "widgetType='" + widgetType + '\'' +
                ", labels=" + labels +
                ", series=" + series +
                ", meta=" + meta +
                '}';
    }
}