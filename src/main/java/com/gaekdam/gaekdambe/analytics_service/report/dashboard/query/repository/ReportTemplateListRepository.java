package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportTemplate;

public interface ReportTemplateListRepository extends JpaRepository<ReportTemplate, Long> {
    List<ReportTemplate> findByEmployeeCode(Long employeeCode);
    List<ReportTemplate> findByIsActiveTrueOrderByTemplateIdAsc();
}
