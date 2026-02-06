package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.VisibilityScope;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportLayoutResponseDto {
  private Long layoutId;
  private Long employeeCode;
  private String name;
  private String description;
  private Boolean isDefault;
  private Boolean isArchived;
  private VisibilityScope visibilityScope;
  private String dateRangePreset;

  // 변경: JsonNode 타입 (클라이언트에 구조 그대로 응답)
  private JsonNode defaultFilterJson;

  private String createdAt;
  private String updatedAt;
}