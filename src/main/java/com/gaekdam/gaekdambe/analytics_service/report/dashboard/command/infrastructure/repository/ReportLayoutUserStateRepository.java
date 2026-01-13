package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportLayoutUserState;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportLayoutUserStateId;

public interface ReportLayoutUserStateRepository extends JpaRepository<ReportLayoutUserState, ReportLayoutUserStateId> {
}
