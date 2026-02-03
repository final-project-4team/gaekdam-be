package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.ReportLayoutListQueryDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.ReportLayoutResponseDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.ReportLayoutTemplateItemDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.ReportTemplateWidgetItemDto;

@Mapper
public interface ReportLayoutQueryMapper {
    ReportLayoutResponseDto findById(@Param("layoutId") Long layoutId);
    List<ReportLayoutResponseDto> findByQuery(@Param("q") ReportLayoutListQueryDto q);
    int countByQuery(@Param("q") ReportLayoutListQueryDto q);
    
    List<ReportLayoutTemplateItemDto> selectTemplatesByLayoutId(@Param("layoutId") Long layoutId);
    Long selectInitialTemplateId(@Param("layoutId") Long layoutId);

    List<ReportTemplateWidgetItemDto> selectWidgetsByTemplateId(@Param("templateId") Long templateId);

}