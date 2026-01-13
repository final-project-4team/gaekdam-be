package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportLayoutWidget;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.mapper.ReportLayoutWidgetMapper;

@Service
public class ReportLayoutWidgetCommandService {
    private final ReportLayoutWidgetMapper mapper;

    public ReportLayoutWidgetCommandService(ReportLayoutWidgetMapper mapper) {
        this.mapper = mapper;
    }

    public int create(ReportLayoutWidget widget) {
        widget.setCreatedAt(LocalDateTime.now());
        widget.setUpdatedAt(LocalDateTime.now());
        return mapper.insert(widget);
    }

    public ReportLayoutWidget findById(Long id) {
        return mapper.findById(id);
    }

    public int update(ReportLayoutWidget widget) {
        widget.setUpdatedAt(LocalDateTime.now());
        return mapper.update(widget);
    }

    public int delete(Long id) {
        return mapper.delete(id);
    }
}
