package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.service;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportTemplate;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.mapper.ReportTemplateMapper;
import org.springframework.stereotype.Service;

@Service
public class ReportTemplateCommandService {
    private final ReportTemplateMapper mapper;

    public ReportTemplateCommandService(ReportTemplateMapper mapper) {
        this.mapper = mapper;
    }

    public int createTemplate(ReportTemplate template) {
        return mapper.insert(template);
    }

    public int updateTemplate(ReportTemplate template) {
        return mapper.update(template);
    }

    public int deleteTemplate(Long templateId) {
        return mapper.delete(templateId);
    }
}
