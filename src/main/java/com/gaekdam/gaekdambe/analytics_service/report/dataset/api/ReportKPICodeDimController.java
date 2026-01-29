package com.gaekdam.gaekdambe.analytics_service.report.dataset.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gaekdam.gaekdambe.analytics_service.report.dataset.query.dto.KpiCodeDto;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.ReportKPICodeDimService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/report")
@RequiredArgsConstructor
public class ReportKPICodeDimController {

    private final ReportKPICodeDimService service; // 또는 repo 직접 주입


    @GetMapping("/kpi-codes")
    //@PreAuthorize("hasAuthority('SETTING_OBJECTIVE_LIST')")
    public ResponseEntity<ApiResponse<List<KpiCodeDto>>> list() {
        List<KpiCodeDto> list = service.listActive();
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
