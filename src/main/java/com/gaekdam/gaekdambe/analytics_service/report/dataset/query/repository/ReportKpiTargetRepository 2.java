package com.gaekdam.gaekdambe.analytics_service.report.dataset.query.repository;

public interface ReportKpiTargetRepository extends JpaRepository<ReportKpiTarget, Long> {
    Optional<ReportKpiTarget> findFirstByMetricKeyAndPeriod(String metricKey, String period);
    Optional<ReportKpiTarget> findFirstByMetricKeyOrderByCreatedAtDesc(String metricKey);
}