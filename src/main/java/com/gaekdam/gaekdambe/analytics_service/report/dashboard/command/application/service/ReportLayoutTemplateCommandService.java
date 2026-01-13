package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportLayoutTemplate;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.mapper.ReportLayoutTemplateMapper;

@Service
public class ReportLayoutTemplateCommandService {
    private final ReportLayoutTemplateMapper mapper;

    public ReportLayoutTemplateCommandService(ReportLayoutTemplateMapper mapper) {
        this.mapper = mapper;
    }

    public int create(ReportLayoutTemplate template) {
        template.setCreatedAt(LocalDateTime.now());
        return mapper.insert(template);
    }

    public ReportLayoutTemplate findById(String id) {
        return mapper.findById(id);
    }

    public int update(ReportLayoutTemplate template) {
        return mapper.update(template);
    }

    public int delete(String id) {
        return mapper.delete(id);
    }
}
