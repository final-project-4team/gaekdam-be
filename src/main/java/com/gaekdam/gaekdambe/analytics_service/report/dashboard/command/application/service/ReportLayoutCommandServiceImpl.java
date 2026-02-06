package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.service;

import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.AuditLog;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto.ReportLayoutCreateDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto.ReportLayoutUpdateDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportLayout;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.infrastructure.repository.ReportLayoutRepository;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;

@Service
@Transactional
public class ReportLayoutCommandServiceImpl implements ReportLayoutCommandService {

    private final ReportLayoutRepository repository;
    private final ObjectMapper objectMapper;

    public ReportLayoutCommandServiceImpl(ReportLayoutRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    @AuditLog(details = "레이아웃 생성", type = PermissionTypeKey.REPORT_LAYOUT_CREATE)
    public Long create(ReportLayoutCreateDto dto) {
        ReportLayout entity = new ReportLayout();
        entity.setEmployeeCode(dto.getEmployeeCode());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setIsDefault(dto.getIsDefault());
        entity.setIsArchived(false);
        entity.setVisibilityScope(dto.getVisibilityScope());
        entity.setDateRangePreset(dto.getDateRangePreset());
        
        if (dto.getDefaultFilterJson() != null) {
            try {
                entity.setDefaultFilterJson(objectMapper.writeValueAsString(dto.getDefaultFilterJson()));
            } catch (JsonProcessingException e) {
                // throw new IllegalArgumentException("defaultFilterJson is not serializable as JSON", e);
                throw new CustomException(ErrorCode.REPORT_LAYOUT_CREATE_ERROR);
            }
        } else {
            entity.setDefaultFilterJson(null);
        }

        ReportLayout saved = repository.save(entity);
        return saved.getLayoutId();
    }

    @Override
    public void update(ReportLayoutUpdateDto dto) {
        ReportLayout entity = repository.findById(dto.getLayoutId())
            .orElseThrow(() -> new IllegalArgumentException("ReportLayout not found: " + dto.getLayoutId()));

        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        if (dto.getIsDefault() != null) entity.setIsDefault(dto.getIsDefault());
        if (dto.getIsArchived() != null) entity.setIsArchived(dto.getIsArchived());
        if (dto.getVisibilityScope() != null) entity.setVisibilityScope(dto.getVisibilityScope());
        if (dto.getDateRangePreset() != null) entity.setDateRangePreset(dto.getDateRangePreset());

        if (dto.getDefaultFilterJson() != null) {
            try {
                entity.setDefaultFilterJson(objectMapper.writeValueAsString(dto.getDefaultFilterJson()));
            } catch (JsonProcessingException e) {
                throw new CustomException(ErrorCode.REPORT_LAYOUT_UPDATE_ERROR);
            }
        } 

        repository.save(entity);
    }

    @Override
    @AuditLog(details = "레이아웃 삭제", type = PermissionTypeKey.REPORT_LAYOUT_DELETE)
    public void delete(Long layoutId) {
        // Optionally check existence first to give clearer error
        if (!repository.existsById(layoutId)) {
            throw new CustomException(ErrorCode.REPORT_LAYOUT_DELETE_ERROR);
        }
        repository.deleteById(layoutId);
    }
}