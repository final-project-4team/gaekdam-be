package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportKPITarget;

@Mapper
public interface ReportKPITargetMapper {
    ReportKPITarget findById(String targetId);
}
