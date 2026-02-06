package com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.service;


import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.ReportLayoutListQueryDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.ReportLayoutResponseDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.ReportLayoutTemplateItemDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.dto.ReportLayoutTemplateListResponseDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.mapper.ReportLayoutQueryMapper;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportLayoutQueryServiceImpl implements ReportLayoutQueryService {
    
    private final ReportLayoutQueryMapper mapper;

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

    @Override
    public ReportLayoutTemplateListResponseDto getTemplatesByLayoutId(Long layoutId) {

        // 1) 템플릿 목록
        List<ReportLayoutTemplateItemDto> templates = mapper.selectTemplatesByLayoutId(layoutId);

        // 레이아웃은 있는데 템플릿이 없는 경우도 있을 수 있으니 null 허용
        Long initialTemplateId = mapper.selectInitialTemplateId(layoutId);

        return ReportLayoutTemplateListResponseDto.builder()
            .layoutId(layoutId)
            .initialTemplateId(initialTemplateId)
            .templates(templates)
            .build();
    }
}
