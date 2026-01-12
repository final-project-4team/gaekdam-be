package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.mapper;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportLayout;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReportLayoutMapper {
    int insert(ReportLayout layout);
    ReportLayout findById(Long layoutId);
    List<ReportLayout> findByEmployeeCode(Long employeeCode);
    int update(ReportLayout layout);
    int delete(Long layoutId);
}
