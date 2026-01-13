package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.mapper;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportLayoutTemplate;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReportLayoutTemplateMapper {
    int insert(ReportLayoutTemplate layoutTemplate);
    ReportLayoutTemplate findById(String layoutTemplateId);
    List<ReportLayoutTemplate> findByLayoutId(Long layoutId);
    int update(ReportLayoutTemplate layoutTemplate);
    int delete(String layoutTemplateId);
}
