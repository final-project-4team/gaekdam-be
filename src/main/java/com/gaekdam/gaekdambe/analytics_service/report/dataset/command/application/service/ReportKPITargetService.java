package com.gaekdam.gaekdambe.analytics_service.report.dataset.command.application.service;


import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.application.dto.ReportKPITargetCreateDto;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.application.dto.ReportKPITargetResponseDto;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.application.dto.ReportKPITargetUpdateDto;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPITarget;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPITargetId;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.infrastructure.repository.ReportKPICodeDimRepository;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.infrastructure.repository.ReportKPITargetRepository;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportKPITargetService {

    private final ReportKPITargetRepository targetRepo;
    private final ReportKPICodeDimRepository kpiRepo;


  //@AuditLog(details = "", type = PermissionTypeKey.KPI_CREATE)
    public ReportKPITargetId create(ReportKPITargetCreateDto dto) {
        // 1) KPI 코드 존재 검증
        if (!kpiRepo.existsById(dto.getKpiCode())) {
            throw new CustomException(ErrorCode.REPORT_KPI_CODE_NOT_FOUND);
        }

        // 2) 기간값 기본 검증(가벼운 수준)
        validatePeriod(dto.getPeriodType(), dto.getPeriodValue());

        ReportKPITargetId id = new ReportKPITargetId(dto.getTargetId(), dto.getHotelGroupCode());

        // 3) 중복 생성 방지
        if (targetRepo.existsById(id)) {
            throw new CustomException(ErrorCode.REPORT_KPI_TARGET_ALREADY_EXISTS);
        }

        ReportKPITarget entity = new ReportKPITarget();
        entity.setId(id);
        entity.setKpiCode(dto.getKpiCode());
        entity.setPeriodType(dto.getPeriodType());
        entity.setPeriodValue(dto.getPeriodValue());
        entity.setTargetValue(dto.getTargetValue());
        entity.setWarningThreshold(dto.getWarningThreshold());
        entity.setDangerThreshold(dto.getDangerThreshold());
        entity.setSeasonType(dto.getSeasonType());
        entity.setEffectiveFrom(dto.getEffectiveFrom());
        entity.setEffectiveTo(dto.getEffectiveTo());

        try {
            targetRepo.save(entity);
            return id;
        } catch (DataIntegrityViolationException e) {
            // FK/UK/NOT NULL 등 DB 제약 위반
            throw new CustomException(ErrorCode.REPORT_KPI_TARGET_CREATE_ERROR);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.REPORT_KPI_TARGET_CREATE_ERROR);
        }
    }

    @Transactional(readOnly = true)

    public ReportKPITargetResponseDto get(String targetId, Long hotelGroupCode) {
        ReportKPITargetId id = new ReportKPITargetId(targetId, hotelGroupCode);
        ReportKPITarget entity = targetRepo.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.REPORT_KPI_TARGET_NOT_FOUND));

        return toResponse(entity);
    }

    public void update(String targetId, Long hotelGroupCode, ReportKPITargetUpdateDto dto) {
        ReportKPITargetId id = new ReportKPITargetId(targetId, hotelGroupCode);
        ReportKPITarget entity = targetRepo.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.REPORT_KPI_TARGET_NOT_FOUND));

        // (선택) periodType/periodValue 둘 중 하나라도 들어오면 검증
        if (dto.getPeriodType() != null || dto.getPeriodValue() != null) {
            validatePeriod(
                dto.getPeriodType() != null ? dto.getPeriodType() : entity.getPeriodType(),
                dto.getPeriodValue() != null ? dto.getPeriodValue() : entity.getPeriodValue()
            );
        }

        // 부분 업데이트
        if (dto.getPeriodType() != null) entity.setPeriodType(dto.getPeriodType());
        if (dto.getPeriodValue() != null) entity.setPeriodValue(dto.getPeriodValue());
        if (dto.getTargetValue() != null) entity.setTargetValue(dto.getTargetValue());
        if (dto.getWarningThreshold() != null) entity.setWarningThreshold(dto.getWarningThreshold());
        if (dto.getDangerThreshold() != null) entity.setDangerThreshold(dto.getDangerThreshold());
        if (dto.getSeasonType() != null) entity.setSeasonType(dto.getSeasonType());
        if (dto.getEffectiveFrom() != null) entity.setEffectiveFrom(dto.getEffectiveFrom());
        if (dto.getEffectiveTo() != null) entity.setEffectiveTo(dto.getEffectiveTo());

        try {
            targetRepo.save(entity);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.REPORT_KPI_TARGET_UPDATE_ERROR);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.REPORT_KPI_TARGET_UPDATE_ERROR);
        }
    }

    //@AuditLog(details = "", type = PermissionTypeKey.KPI_DELETE)
    public void delete(String targetId, Long hotelGroupCode) {
        ReportKPITargetId id = new ReportKPITargetId(targetId, hotelGroupCode);

        // 존재 확인
        if (!targetRepo.existsById(id)) {
            throw new CustomException(ErrorCode.REPORT_KPI_TARGET_NOT_FOUND);
        }

        try {
            targetRepo.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.REPORT_KPI_TARGET_DELETE_ERROR);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.REPORT_KPI_TARGET_DELETE_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public List<ReportKPITargetResponseDto> list(Long hotelGroupCode, String kpiCode) {
        var list = (kpiCode == null || kpiCode.isBlank())
            ? targetRepo.findByIdHotelGroupCode(hotelGroupCode)
            : targetRepo.findByIdHotelGroupCodeAndKpiCode(hotelGroupCode, kpiCode);

        return list.stream().map(this::toResponse).toList();
    }

    private void validatePeriod(String periodType, String periodValue) {
        // periodType: "MONTH" or "YEAR"
        if (periodType == null || periodValue == null) {
            throw new CustomException(ErrorCode.REPORT_KPI_TARGET_INVALID_PERIOD);
        }

        String pt = periodType.trim().toUpperCase();
        if (!pt.equals("MONTH") && !pt.equals("YEAR")) {
            throw new CustomException(ErrorCode.REPORT_KPI_TARGET_INVALID_PERIOD);
        }

        // YEAR=YYYY, MONTH=YYYY-MM (엄격한 정규식)
        if (pt.equals("YEAR")) {
            if (!periodValue.matches("^\\d{4}$")) {
                throw new CustomException(ErrorCode.REPORT_KPI_TARGET_INVALID_PERIOD);
            }
        } else { // MONTH
            if (!periodValue.matches("^\\d{4}-(0[1-9]|1[0-2])$")) {
                throw new CustomException(ErrorCode.REPORT_KPI_TARGET_INVALID_PERIOD);
            }
        }
    }

    private ReportKPITargetResponseDto toResponse(ReportKPITarget e) {
        return ReportKPITargetResponseDto.builder()
            .targetId(e.getId().getTargetId())
            .hotelGroupCode(e.getId().getHotelGroupCode())
            .kpiCode(e.getKpiCode())
            .periodType(e.getPeriodType())
            .periodValue(e.getPeriodValue())
            .targetValue(e.getTargetValue() == null ? null : e.getTargetValue().toPlainString())
            .warningThreshold(e.getWarningThreshold() == null ? null : e.getWarningThreshold().toPlainString())
            .dangerThreshold(e.getDangerThreshold() == null ? null : e.getDangerThreshold().toPlainString())
            .seasonType(e.getSeasonType())
            .effectiveFrom(e.getEffectiveFrom() == null ? null : e.getEffectiveFrom().toString())
            .effectiveTo(e.getEffectiveTo() == null ? null : e.getEffectiveTo().toString())
            .createdAt(e.getCreatedAt() == null ? null : e.getCreatedAt().toString())
            .updatedAt(e.getUpdatedAt() == null ? null : e.getUpdatedAt().toString())
            .build();
    }
}
