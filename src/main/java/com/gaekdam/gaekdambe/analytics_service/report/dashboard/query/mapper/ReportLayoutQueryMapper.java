package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto.ReportLayoutListQueryDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto.ReportLayoutResponseDto;

@Mapper
public interface ReportLayoutQueryMapper {
    ReportLayoutResponseDto findById(@Param("layoutId") Long layoutId);
    List<ReportLayoutResponseDto> findByQuery(@Param("q") ReportLayoutListQueryDto q);
    int countByQuery(@Param("q") ReportLayoutListQueryDto q);
}