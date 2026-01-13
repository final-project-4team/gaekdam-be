package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportTemplateWidget;

@Mapper
public interface ReportTemplateWidgetMapper {
    int insert(ReportTemplateWidget widget);
    ReportTemplateWidget findById(String templateWidgetId);
    List<ReportTemplateWidget> findByTemplateId(Long templateId);
    int update(ReportTemplateWidget widget);
    int delete(String templateWidgetId);
}
