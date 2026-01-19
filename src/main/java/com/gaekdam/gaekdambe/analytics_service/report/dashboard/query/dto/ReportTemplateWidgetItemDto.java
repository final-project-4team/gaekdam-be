package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportTemplateWidgetItemDto {
  private Long templateWidgetId;
  private Long templateId;

  private String widgetType;     // KPI_CARD, LINE, BAR, TABLE, GAUGE
  private String title;

  private String datasetType;    // CX/OPS/CUST/REV
  private String metricKey;
  private String dimensionKey;

  private String defaultPeriod;  // MONTH/YEAR
  private Integer defaultSortOrder;

  private Object optionsJson;        // 일단 String으로 내려도 됨 (원하면 JsonNode로 바꿔도 됨)
  private Object defaultFilterJson;

  private String createdAt;
  private String updatedAt;
}
