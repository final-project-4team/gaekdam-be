package com.gaekdam.gaekdambe.analytics_service.report.dataset.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/report/kpi-targets")
public class ReportKPITargetController {

    private final ReportKPITargetService service;

    @PostMapping
    public ResponseEntity<ApiResponse<ReportKPITargetId>> create(@RequestBody @Valid ReportKPITargetCreateDto dto) {
        ReportKPITargetId id = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(id));
    }

    @GetMapping("/{hotelGroupCode}/{targetId}")

    public ResponseEntity<ApiResponse<ReportKPITargetResponseDto>> get(
        @PathVariable Long hotelGroupCode,
        @PathVariable String targetId
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.get(targetId, hotelGroupCode)));
    }

    @GetMapping
    //@AuditLog(details = "", type = PermissionTypeKey.KPI_LIST)
    public ResponseEntity<ApiResponse<List<ReportKPITargetResponseDto>>> list(
        @RequestParam Long hotelGroupCode,
        @RequestParam(required = false) String kpiCode
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.list(hotelGroupCode, kpiCode)));
    }

    @PatchMapping("/{hotelGroupCode}/{targetId}")
    public ResponseEntity<ApiResponse<Void>> update(
        @PathVariable Long hotelGroupCode,
        @PathVariable String targetId,
        @RequestBody @Valid ReportKPITargetUpdateDto dto
    ) {
        service.update(targetId, hotelGroupCode, dto);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/{hotelGroupCode}/{targetId}")
    public ResponseEntity<ApiResponse<Void>> delete(
        @PathVariable Long hotelGroupCode,
        @PathVariable String targetId
    ) {
        service.delete(targetId, hotelGroupCode);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
