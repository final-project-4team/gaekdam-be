package com.gaekdam.gaekdambe.analytics_service.report.dataset.query.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPITarget;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPITargetId;

public interface ReportKpiTargetRepository extends JpaRepository<ReportKPITarget, ReportKPITargetId> {
    Optional<ReportKPITarget> findFirstByKpiCodeAndPeriodValue(String kpiCode, String periodValue);
    Optional<ReportKPITarget> findFirstByKpiCodeOrderByCreatedAtDesc(String kpiCode);

}