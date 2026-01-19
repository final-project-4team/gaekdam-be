package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.ReportTemplateType;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportTemplate;

public interface ReportTemplateRepository extends JpaRepository<ReportTemplate, Long> {
    boolean existsByTemplateType(ReportTemplateType templateType);
    Optional<ReportTemplate> findByTemplateType(ReportTemplateType templateType);
    List<ReportTemplate> findByIsActiveTrueOrderByTemplateIdAsc();

}
