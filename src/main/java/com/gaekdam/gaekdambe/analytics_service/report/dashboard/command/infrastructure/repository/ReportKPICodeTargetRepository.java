package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportKPITarget;

@Repository
public interface ReportKPICodeTargetRepository extends JpaRepository<ReportKPITarget, String> {
    
}
