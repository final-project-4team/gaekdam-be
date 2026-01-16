package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportLayoutListQueryDto {
    private Long employeeCode;
    private String name;
    private Integer offset;
    private Integer limit;
}