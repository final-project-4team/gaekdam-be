package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto.ReportLayoutListQueryDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto.ReportLayoutResponseDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.mapper.ReportLayoutQueryMapper;

@Service
@Transactional(readOnly = true)
public class ReportLayoutQueryServiceImpl implements ReportLayoutQueryService {
    
    private final ReportLayoutQueryMapper mapper;

    public ReportLayoutQueryServiceImpl(ReportLayoutQueryMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public ReportLayoutResponseDto getById(Long layoutId) {
        return mapper.findById(layoutId);
    }

    @Override
    public List<ReportLayoutResponseDto> list(ReportLayoutListQueryDto q) {
        return mapper.findByQuery(q);
    }

    @Override
    public int count(ReportLayoutListQueryDto q) {
        return mapper.countByQuery(q);
    }
}
