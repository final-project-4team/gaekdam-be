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
public class ReportLayoutDetailResponseDto {
  private Long layoutId;
  private String layoutName;

  private Long initialTemplateId;                 // 첫 템플릿(자동 활성화 대상)
  private List<ReportLayoutTemplateDetailDto> templates; // 좌측 템플릿 목록 + (필요시) 위젯
}

