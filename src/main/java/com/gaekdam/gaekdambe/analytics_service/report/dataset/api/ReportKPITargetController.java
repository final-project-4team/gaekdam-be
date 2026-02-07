package com.gaekdam.gaekdambe.analytics_service.report.dataset.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.application.dto.ReportKPITargetCreateDto;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.application.dto.ReportKPITargetResponseDto;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.application.dto.ReportKPITargetUpdateDto;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.application.service.ReportKPITargetService;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.command.domain.entity.ReportKPITargetId;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.swagger.SpecResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "KPI 목표 설정")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/report/kpi-targets")
public class ReportKPITargetController {

    private final ReportKPITargetService service;

    @PostMapping
    @PreAuthorize("hasAuthority('SETTING_OBJECTIVE_UPDATE')")
    @Operation(summary = "KPI 목표 생성", description = "새로운 KPI 목표를 생성합니다.")
    @SpecResponse( description = "생성 성공")
    public ResponseEntity<ApiResponse<ReportKPITargetId>> create(@RequestBody @Valid ReportKPITargetCreateDto dto) {
        ReportKPITargetId id = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(id));
    }

    @GetMapping("/{hotelGroupCode}/{targetId}")
    @PreAuthorize("hasAuthority('SETTING_OBJECTIVE_LIST')")
    @Operation(summary = "KPI 목표 상세 조회", description = "특정 KPI 목표 정보를 조회합니다.")
    @SpecResponse(responseCode = "200", description = "조회 성공")
    public ResponseEntity<ApiResponse<ReportKPITargetResponseDto>> get(
            @Parameter(description = "호텔 그룹 코드") @PathVariable Long hotelGroupCode,
            @Parameter(description = "목표 ID") @PathVariable String targetId) {
        return ResponseEntity.ok(ApiResponse.success(service.get(targetId, hotelGroupCode)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SETTING_OBJECTIVE_LIST')")
    @Operation(summary = "KPI 목표 목록 조회", description = "호텔 그룹별 KPI 목표 목록을 조회합니다.")
    @SpecResponse(responseCode = "200", description = "조회 성공")
    public ResponseEntity<ApiResponse<List<ReportKPITargetResponseDto>>> list(
            @Parameter(description = "호텔 그룹 코드") @RequestParam Long hotelGroupCode,
            @Parameter(description = "KPI 코드 (필터)") @RequestParam(required = false) String kpiCode) {
        return ResponseEntity.ok(ApiResponse.success(service.list(hotelGroupCode, kpiCode)));
    }

    @PatchMapping("/{hotelGroupCode}/{targetId}")
    @PreAuthorize("hasAuthority('SETTING_OBJECTIVE_UPDATE')")
    @Operation(summary = "KPI 목표 수정", description = "기존 KPI 목표 정보를 수정합니다.")
    @SpecResponse(responseCode = "200", description = "수정 성공")
    public ResponseEntity<ApiResponse<Void>> update(
            @Parameter(description = "호텔 그룹 코드") @PathVariable Long hotelGroupCode,
            @Parameter(description = "목표 ID") @PathVariable String targetId,
            @RequestBody @Valid ReportKPITargetUpdateDto dto) {
        service.update(targetId, hotelGroupCode, dto);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/{hotelGroupCode}/{targetId}")
    @PreAuthorize("hasAuthority('SETTING_OBJECTIVE_DELETE')")
    @Operation(summary = "KPI 목표 삭제", description = "기본 KPI 목표를 삭제합니다.")
    @SpecResponse(responseCode = "200", description = "삭제 성공")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "호텔 그룹 코드") @PathVariable Long hotelGroupCode,
            @Parameter(description = "목표 ID") @PathVariable String targetId) {
        service.delete(targetId, hotelGroupCode);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
