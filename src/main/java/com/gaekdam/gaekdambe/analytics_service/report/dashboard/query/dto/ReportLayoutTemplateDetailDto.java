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
public class ReportLayoutTemplateDetailDto {
  private Long layoutTemplateId;     // 연결테이블 PK
  private Long templateId;           // 라이브러리 템플릿ID
  private String templateType;       // SUMMARY_ALL ...
  private String templateName;       // 라이브러리 기본명
  private String displayName;        // 레이아웃 내 사용자 지정명 (ReportLayoutTemplate.display_name)
  private Integer sortOrder;
  private Boolean isActive;

  private List<ReportTemplateWidgetItemDto> widgets; // ⭐ 상세조회에서는 "초기 템플릿"만 채워서 내려도 됨
}
