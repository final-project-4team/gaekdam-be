package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.mapper;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportCUSTKPIFact;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReportCUSTKPIFactMapper {
    ReportCUSTKPIFact findByDateAndGroup(java.time.LocalDate kpiDate, Long hotelGroupCode);
}
