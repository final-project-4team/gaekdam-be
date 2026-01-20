package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportLayoutTemplate;

public interface ReportLayoutTemplateRepository extends JpaRepository<ReportLayoutTemplate, Long> {
  boolean existsByLayoutIdAndTemplateId(Long layoutId, Long templateId);
  List<ReportLayoutTemplate> findByLayoutIdAndIsActiveTrueOrderBySortOrderAscCreatedAtAsc(Long layoutId);
  Optional<ReportLayoutTemplate> findByLayoutIdAndTemplateId(Long layoutId, Long templateId);
}
