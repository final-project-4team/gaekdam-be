package com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPITarget;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPITargetId;

public interface ReportKPITargetRepository extends JpaRepository<ReportKPITarget, ReportKPITargetId> {

    List<ReportKPITarget> findByIdHotelGroupCode(Long hotelGroupCode);
    List<ReportKPITarget> findByIdHotelGroupCodeAndKpiCode(Long hotelGroupCode, String kpiCode);

    // Refer to embedded id property using id_<fieldName>
    Optional<ReportKPITarget> findById_HotelGroupCodeAndKpiCodeAndPeriodTypeAndPeriodValue(
        Long hotelGroupCode, String kpiCode, String periodType, String periodValue);

    // JQuery 이용
    // 연도 자동탐지용 - DB 에 존재하는 연도 목록을 뽑음
    @Query("select distinct substring(t.periodValue,1,4) from ReportKPITarget t where t.id.hotelGroupCode = :hotelGroupCode")
    List<String> findDistinctYearsByHotelGroupCode(@Param("hotelGroupCode") Long hotelGroupCode);

    // 연도(연간 + 월간 모두) 대상 타깃들을 한 번에 가져오는 쿼리:
    @Query("select t from ReportKPITarget t where t.id.hotelGroupCode = :hotelGroupCode and (t.periodType = 'YEAR' and t.periodValue = :year) or (t.periodType = 'MONTH' and t.periodValue like concat(:year, '-%'))")
    List<ReportKPITarget> findTargetsForYear(@Param("hotelGroupCode") Long hotelGroupCode, @Param("year") String year);
}