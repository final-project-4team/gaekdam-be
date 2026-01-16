package com.gaekdam.gaekdambe.analytics_service.report.dataset.query.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportOPSKPIFact;

@Mapper
public interface ReportOPSKPIFactMapper {
    ReportOPSKPIFact findByDateAndGroup(java.time.LocalDate kpiDate, Long hotelGroupCode);
}
