package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportTemplateWidget;

public interface ReportTemplateWidgetQueryRepository extends JpaRepository<ReportTemplateWidget, Long> {
    List<ReportTemplateWidget> findByTemplateIdOrderByDefaultSortOrderAsc(Long templateId);
}
