package com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPICodeDim;

public interface ReportKPICodeDimRepository extends JpaRepository<ReportKPICodeDim, String> {
    // 필요하면 추가:
    // List<ReportKPICodeDim> findByDomainTypeAndIsActiveTrue(String domainType);
    List<ReportKPICodeDim> findByIsActiveTrueOrderByKpiCodeAsc();

    Optional<ReportKPICodeDim> findByKpiCode(String kpiCode);
}
