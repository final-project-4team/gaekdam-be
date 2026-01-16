package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportLayout;

public interface ReportLayoutRepository extends JpaRepository<ReportLayout, Long> {
}
