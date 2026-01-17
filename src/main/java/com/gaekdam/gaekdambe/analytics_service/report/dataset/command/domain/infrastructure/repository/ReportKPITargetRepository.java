package com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPITarget;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPITargetId;

public interface ReportKPITargetRepository extends JpaRepository<ReportKPITarget, ReportKPITargetId> {

    List<ReportKPITarget> findByIdHotelGroupCode(Long hotelGroupCode);
    List<ReportKPITarget> findByIdHotelGroupCodeAndKpiCode(Long hotelGroupCode, String kpiCode);

    Optional<ReportKPITarget> findByIdHotelGroupCodeAndKpiCodeAndPeriodTypeAndPeriodValue(
            Long hotelGroupCode,
            String kpiCode,
            String periodType,
            String periodValue
    );
}