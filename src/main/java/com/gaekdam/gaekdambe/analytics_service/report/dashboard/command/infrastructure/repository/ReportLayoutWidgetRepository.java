package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportLayoutWidget;

public interface ReportLayoutWidgetRepository extends JpaRepository<ReportLayoutWidget, Long> {
}
