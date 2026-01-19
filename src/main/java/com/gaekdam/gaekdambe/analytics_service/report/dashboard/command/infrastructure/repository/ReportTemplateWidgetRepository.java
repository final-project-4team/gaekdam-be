package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.infrastructure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportTemplateWidget;

public interface ReportTemplateWidgetRepository extends JpaRepository<ReportTemplateWidget, Long> {
    long countByTemplateId(Long templateId);
    List<ReportTemplateWidget> findByTemplateIdOrderByDefaultSortOrderAsc(Long templateId);
    List<ReportTemplateWidget> findByTemplateIdOrderByDefaultSortOrderAscTemplateWidgetIdAsc(Long templateId);
    boolean existsByTemplateId(Long templateId);
    boolean existsByTemplateIdAndMetricKeyAndWidgetType(Long templateId, String metricKey, String widgetType);
}
