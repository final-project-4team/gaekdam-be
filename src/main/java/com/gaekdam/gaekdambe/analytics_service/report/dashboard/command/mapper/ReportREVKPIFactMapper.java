package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.mapper;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportREVKPIFact;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReportREVKPIFactMapper {
    ReportREVKPIFact findByDateAndGroup(java.time.LocalDate kpiDate, Long hotelGroupCode);
}
