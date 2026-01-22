package com.gaekdam.gaekdambe.analytics_service.report.dataset.query.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KpiCodeDto {
  private String kpiCode;
  private String kpiName;
  private String unit;
  private String description;
}
