package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportLayoutUserState;

@Mapper
public interface ReportLayoutUserStateMapper {
    int insert(ReportLayoutUserState state);
    ReportLayoutUserState findByLayoutAndEmployee(Long layoutId, Long employeeCode);
    int update(ReportLayoutUserState state);
}
