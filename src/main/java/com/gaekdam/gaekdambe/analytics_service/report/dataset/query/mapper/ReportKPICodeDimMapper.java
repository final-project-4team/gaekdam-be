package com.gaekdam.gaekdambe.analytics_service.report.dataset.query.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPICodeDim;

@Mapper
public interface ReportKPICodeDimMapper {
    ReportKPICodeDim findByCode(String kpiCode);
    List<ReportKPICodeDim> findAllActive();
}
