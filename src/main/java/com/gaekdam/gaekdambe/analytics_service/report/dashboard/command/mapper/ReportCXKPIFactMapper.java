package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.mapper;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportCXKPIFact;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReportCXKPIFactMapper {
    ReportCXKPIFact findByDateAndGroup(java.time.LocalDate kpiDate, Long hotelGroupCode);
}
