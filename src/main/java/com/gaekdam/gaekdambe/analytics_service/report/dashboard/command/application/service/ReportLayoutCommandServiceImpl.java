package com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto.ReportLayoutCreateDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.application.dto.ReportLayoutUpdateDto;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportLayout;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.infrastructure.repository.ReportLayoutRepository;

@Service
@Transactional
public class ReportLayoutCommandServiceImpl implements ReportLayoutCommandService {

    private final ReportLayoutRepository repository;

    public ReportLayoutCommandServiceImpl(ReportLayoutRepository repository) {
        this.repository = repository;
    }

    @Override
    public Long create(ReportLayoutCreateDto dto) {
        ReportLayout entity = new ReportLayout();
        entity.setEmployeeCode(dto.getEmployeeCode());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setIsDefault(dto.getIsDefault());
        entity.setIsArchived(false);
        entity.setVisibilityScope(dto.getVisibilityScope());
        entity.setDateRangePreset(dto.getDateRangePreset());
        entity.setDefaultFilterJson(dto.getDefaultFilterJson());
        entity.setLayoutJson(dto.getLayoutJson());
        // TODO: handle business rule for isDefault (e.g., unset other defaults) if required
        ReportLayout saved = repository.save(entity);
        return saved.getLayoutId();
    }

    @Override
    public void update(ReportLayoutUpdateDto dto) {
        Long id = dto.getLayoutId();
        ReportLayout entity = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("ReportLayout not found: " + id));

        if (dto.getName() != null) entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        if (dto.getIsDefault() != null) entity.setIsDefault(dto.getIsDefault());
        if (dto.getIsArchived() != null) entity.setIsArchived(dto.getIsArchived());
        if (dto.getVisibilityScope() != null) entity.setVisibilityScope(dto.getVisibilityScope());
        if (dto.getDateRangePreset() != null) entity.setDateRangePreset(dto.getDateRangePreset());
        if (dto.getDefaultFilterJson() != null) entity.setDefaultFilterJson(dto.getDefaultFilterJson());
        if (dto.getLayoutJson() != null) entity.setLayoutJson(dto.getLayoutJson());
        // updatedAt is handled by @PreUpdate in the entity
        repository.save(entity);
    }

    @Override
    public void delete(Long layoutId) {
        // Optionally check existence first to give clearer error
        if (!repository.existsById(layoutId)) {
            throw new IllegalArgumentException("ReportLayout not found: " + layoutId);
        }
        repository.deleteById(layoutId);
    }
}