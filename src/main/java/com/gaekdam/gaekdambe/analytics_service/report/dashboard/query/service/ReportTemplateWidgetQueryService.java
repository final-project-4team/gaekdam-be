package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportTemplateWidget;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.infrastructure.repository.ReportTemplateRepository;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.infrastructure.repository.ReportTemplateWidgetRepository;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.ReportTemplateWidgetResponseDto;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportTemplateWidgetQueryService {

    private final ReportTemplateRepository templateRepo;
    private final ReportTemplateWidgetRepository widgetRepo;

    public List<ReportTemplateWidgetResponseDto> listByTemplateId(Long templateId) {

        // 템플릿 존재 검증 (권장)
        if (!templateRepo.existsById(templateId)) {
            throw new CustomException(ErrorCode.REPORT_TEMPLATE_NOT_FOUND);
        }

        return widgetRepo.findByTemplateIdOrderByDefaultSortOrderAscTemplateWidgetIdAsc(templateId)
            .stream()
            .map(this::toDto)
            .toList();
    }

    private ReportTemplateWidgetResponseDto toDto(ReportTemplateWidget w) {
        return ReportTemplateWidgetResponseDto.builder()
            .templateWidgetId(w.getTemplateWidgetId())
            .templateId(w.getTemplateId())
            .widgetType(w.getWidgetType())
            .title(w.getTitle())
            .datasetType(w.getDatasetType())
            .metricKey(w.getMetricKey())
            .dimensionKey(w.getDimensionKey())
            .defaultPeriod(w.getDefaultPeriod())
            .defaultSortOrder(w.getDefaultSortOrder())
            .optionsJson(w.getOptionsJson())
            .defaultFilterJson(w.getDefaultFilterJson())
            .build();
    }
}
