package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportLayoutWidget;

@Mapper
public interface ReportLayoutWidgetMapper {
    int insert(ReportLayoutWidget widget);
    ReportLayoutWidget findById(Long layoutWidgetId);
    List<ReportLayoutWidget> findByLayoutId(Long layoutId);
    int update(ReportLayoutWidget widget);
    int delete(Long layoutWidgetId);
}
