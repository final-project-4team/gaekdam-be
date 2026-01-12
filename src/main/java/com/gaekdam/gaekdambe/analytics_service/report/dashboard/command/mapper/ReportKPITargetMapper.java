package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.mapper;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportKPITarget;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReportKPITargetMapper {
    ReportKPITarget findById(String targetId);
}
