package com.gaekdam.gaekdambe.analytics_service.report.dataset.query.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPITarget;

public interface ReportKpiTargetRepository extends JpaRepository<ReportKPITarget, Long> {
    Optional<ReportKPITarget> findFirstByKpiCodeAndPeriodValue(String kpiCode, String periodValue);
    Optional<ReportKPITarget> findFirstByKpiCodeOrderByCreatedAtDesc(String kpiCode);
}