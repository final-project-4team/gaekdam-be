package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.service;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto.ReportLayoutCreateDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto.ReportLayoutUpdateDto;

public interface ReportLayoutCommandService {
    Long create(ReportLayoutCreateDto dto);
    void update(ReportLayoutUpdateDto dto);
    void delete(Long layoutId);
}