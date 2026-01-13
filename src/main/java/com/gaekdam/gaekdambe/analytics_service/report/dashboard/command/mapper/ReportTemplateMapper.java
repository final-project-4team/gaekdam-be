package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportTemplate;

@Mapper
public interface ReportTemplateMapper {
    int insert(ReportTemplate template);
    ReportTemplate findById(Long templateId);
    List<ReportTemplate> findAllActive();
    int update(ReportTemplate template);
    int delete(Long templateId);
}
