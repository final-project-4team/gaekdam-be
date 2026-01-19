package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.service;

import org.springframework.stereotype.Service;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto.ReportLayoutTemplateCreateDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto.ReportLayoutTemplateUpdateDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportLayoutTemplate;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.infrastructure.repository.ReportLayoutRepository;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.infrastructure.repository.ReportLayoutTemplateRepository;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.infrastructure.repository.ReportTemplateRepository;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportLayoutTemplateCommandService {

    private final ReportLayoutTemplateRepository repo;
    private final ReportTemplateRepository templateRepo; // 라이브러리 검증용(선택)
    private final ReportLayoutRepository layoutRepo;     // 레이아웃 존재 검증용(선택)

    public Long addTemplate(Long layoutId, Long createdBy, ReportLayoutTemplateCreateDto dto) {

        if (!layoutRepo.existsById(layoutId)) {
            throw new CustomException(ErrorCode.REPORT_LAYOUT_NOT_FOUND);
        }
        if (!templateRepo.existsById(dto.getTemplateId())) {
            throw new CustomException(ErrorCode.REPORT_TEMPLATE_NOT_FOUND);
        }
        if (repo.existsByLayoutIdAndTemplateId(layoutId, dto.getTemplateId())) {
            throw new CustomException(ErrorCode.REPORT_LAYOUT_TEMPLATE_DUPLICATE);
        }

        ReportLayoutTemplate e = new ReportLayoutTemplate();
        e.setLayoutId(layoutId);
        e.setTemplateId(dto.getTemplateId());
        e.setCreatedBy(createdBy);
        e.setDisplayName(dto.getDisplayName());
        e.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        e.setIsActive(true);

        return repo.save(e).getLayoutTemplateId();
    }

    public void update(Long layoutId, Long layoutTemplateId, ReportLayoutTemplateUpdateDto dto) {
        ReportLayoutTemplate e = repo.findById(layoutTemplateId)
            .orElseThrow(() -> new CustomException(ErrorCode.REPORT_LAYOUT_TEMPLATE_NOT_FOUND));

        // 안전장치: path의 layoutId와 실제 row의 layoutId 일치 검증
        if (!e.getLayoutId().equals(layoutId)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        if (dto.getDisplayName() != null) e.setDisplayName(dto.getDisplayName());
        if (dto.getSortOrder() != null) e.setSortOrder(dto.getSortOrder());
        if (dto.getIsActive() != null) e.setIsActive(dto.getIsActive());

        repo.save(e);
    }

    public void delete(Long layoutId, Long layoutTemplateId) {
        ReportLayoutTemplate e = repo.findById(layoutTemplateId)
            .orElseThrow(() -> new CustomException(ErrorCode.REPORT_LAYOUT_TEMPLATE_NOT_FOUND));

        if (!e.getLayoutId().equals(layoutId)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 소프트 삭제 추천
        e.setIsActive(false);
        repo.save(e);

        // 물리삭제로 하고 싶으면:
        // repo.delete(e);
    }
}

