package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportTemplateWidget;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.mapper.ReportTemplateWidgetMapper;

@Service
public class ReportTemplateWidgetCommandService {
    private final ReportTemplateWidgetMapper mapper;

    public ReportTemplateWidgetCommandService(ReportTemplateWidgetMapper mapper) {
        this.mapper = mapper;
    }

    public int create(ReportTemplateWidget widget) {
        widget.setCreatedAt(LocalDateTime.now());
        widget.setUpdatedAt(LocalDateTime.now());
        return mapper.insert(widget);
    }

    public ReportTemplateWidget findById(String id) {
        return mapper.findById(id);
    }

    public int update(ReportTemplateWidget widget) {
        widget.setUpdatedAt(LocalDateTime.now());
        return mapper.update(widget);
    }

    public int delete(String id) {
        return mapper.delete(id);
    }
}
