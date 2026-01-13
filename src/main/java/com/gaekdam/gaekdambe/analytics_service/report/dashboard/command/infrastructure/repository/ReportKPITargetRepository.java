package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportKPITarget;

public interface ReportKPITargetRepository extends JpaRepository<ReportKPITarget, String> {
}
