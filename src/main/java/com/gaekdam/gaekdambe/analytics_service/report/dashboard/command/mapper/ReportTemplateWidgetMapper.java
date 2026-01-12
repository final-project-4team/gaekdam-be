package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.mapper;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportTemplateWidget;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReportTemplateWidgetMapper {
    int insert(ReportTemplateWidget widget);
    ReportTemplateWidget findById(String templateWidgetId);
    List<ReportTemplateWidget> findByTemplateId(Long templateId);
    int update(ReportTemplateWidget widget);
    int delete(String templateWidgetId);
}
