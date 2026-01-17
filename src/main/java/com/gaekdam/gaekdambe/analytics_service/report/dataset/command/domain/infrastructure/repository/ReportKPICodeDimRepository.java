package com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPICodeDim;

public interface ReportKPICodeDimRepository extends JpaRepository<ReportKPICodeDim, String> {
    // 필요하면 추가:
    // List<ReportKPICodeDim> findByDomainTypeAndIsActiveTrue(String domainType);
}
