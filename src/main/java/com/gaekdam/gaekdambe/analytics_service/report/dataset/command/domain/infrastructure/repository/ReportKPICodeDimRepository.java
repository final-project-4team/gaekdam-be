package com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPICodeDim;

@Repository
public interface ReportKPICodeDimRepository extends JpaRepository<ReportKPICodeDim, String> {
}
