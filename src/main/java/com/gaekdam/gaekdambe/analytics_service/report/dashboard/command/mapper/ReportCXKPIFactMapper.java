package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportCXKPIFact;

@Mapper
public interface ReportCXKPIFactMapper {
    ReportCXKPIFact findByDateAndGroup(java.time.LocalDate kpiDate, Long hotelGroupCode);
}
