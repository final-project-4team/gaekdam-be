package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.service;

import org.springframework.stereotype.Service;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportLayout;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.mapper.ReportLayoutMapper;

@Service
public class ReportLayoutCommandService {
    private final ReportLayoutMapper mapper;

    public ReportLayoutCommandService(ReportLayoutMapper mapper) {
        this.mapper = mapper;
    }

    public int create(ReportLayout layout) {
        return mapper.insert(layout);
    }

    public ReportLayout findById(Long id) {
        return mapper.findById(id);
    }

    public int update(ReportLayout layout) {
        return mapper.update(layout);
    }

    public int delete(Long id) {
        return mapper.delete(id);
    }
}
