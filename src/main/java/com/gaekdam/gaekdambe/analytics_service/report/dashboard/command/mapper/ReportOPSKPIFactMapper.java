package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.mapper;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportOPSKPIFact;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReportOPSKPIFactMapper {
    ReportOPSKPIFact findByDateAndGroup(java.time.LocalDate kpiDate, Long hotelGroupCode);
}
