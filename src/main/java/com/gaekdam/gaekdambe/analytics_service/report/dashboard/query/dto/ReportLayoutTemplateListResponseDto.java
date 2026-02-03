package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto;

import java.util.List;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportLayoutTemplateListResponseDto {
  private Long layoutId;
  private Long initialTemplateId;
  private List<ReportLayoutTemplateItemDto> templates;
}
