package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.mapper;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportExport;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReportExportMapper {
    int insert(ReportExport export);
    ReportExport findById(String exportId);
    List<ReportExport> findByLayoutId(Long layoutId);
    int update(ReportExport export);
    int delete(String exportId);
}
