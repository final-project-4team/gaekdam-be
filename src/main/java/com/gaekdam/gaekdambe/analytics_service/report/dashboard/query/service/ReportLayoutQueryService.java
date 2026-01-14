package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.service;

import java.util.List;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto.ReportLayoutListQueryDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto.ReportLayoutResponseDto;

public interface ReportLayoutQueryService {
    ReportLayoutResponseDto getById(Long layoutId);
    List<ReportLayoutResponseDto> list(ReportLayoutListQueryDto q);
    int count(ReportLayoutListQueryDto q);
}