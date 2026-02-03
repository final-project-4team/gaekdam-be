package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportLayoutListItemDto {
  private List<ReportLayoutResponseDto> layouts;
}
