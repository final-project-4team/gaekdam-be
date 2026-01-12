package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.mapper;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportKPICodeDim;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReportKPICodeDimMapper {
    ReportKPICodeDim findByCode(String kpiCode);
    List<ReportKPICodeDim> findAllActive();
}
