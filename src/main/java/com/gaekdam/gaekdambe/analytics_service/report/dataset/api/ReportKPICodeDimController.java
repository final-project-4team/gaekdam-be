package com.gaekdam.gaekdambe.analytics_service.report.dataset.api;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gaekdam.gaekdambe.analytics_service.report.dataset.query.dto.KpiCodeDto;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.ReportKPICodeDimService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;

import com.gaekdam.gaekdambe.global.config.swagger.SpecResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "KPI 코드 관리")
@RestController
@RequestMapping("/api/v1/report")
@RequiredArgsConstructor
public class ReportKPICodeDimController {

    private final ReportKPICodeDimService service; // 또는 repo 직접 주입

    @GetMapping("/kpi-codes")
    // @PreAuthorize("hasAuthority('SETTING_OBJECTIVE_LIST')")
    @Operation(summary = "KPI 코드 목록 조회", description = "활성화된 KPI 코드 목록을 조회합니다.")
    @SpecResponse(description = "조회 성공")
    public ResponseEntity<ApiResponse<List<KpiCodeDto>>> list() {
        List<KpiCodeDto> list = service.listActive();
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
