package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportTemplateWidget;

public interface ReportTemplateWidgetRepository extends JpaRepository<ReportTemplateWidget, String> {
}
