package com.gaekdam.gaekdambe.analytics_service.report.dataset.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.MetricQueryService;
import com.gaekdam.gaekdambe.analytics_service.report.dataset.query.service.MetricResult;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;

import lombok.RequiredArgsConstructor;

import com.gaekdam.gaekdambe.global.config.swagger.SpecResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "메트릭 테스트")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/metric")
public class MetricTestController {

    private final MetricQueryService metricQueryService;

    @GetMapping
    @Operation(summary = "메트릭 수치 조회", description = "메트릭명과 기간, 필터 조건을 기반으로 수치를 조회합니다.")
    @SpecResponse( description = "조회 성공")
    public ResponseEntity<ApiResponse<MetricResult>> getMetric(
            @Parameter(description = "메트릭명") @RequestParam String metric,
            @Parameter(description = "기간 값") @RequestParam String periodValue,
            @Parameter(description = "지점 코드") @RequestParam(required = false) Long propertyCode,
            @Parameter(description = "호텔 ID") @RequestParam(required = false) Long hotelId,
            @Parameter(description = "호텔 그룹 코드") @RequestParam(required = false) Long hotelGroupCode) {
        Map<String, Object> filter = new HashMap<>();
        if (propertyCode != null)
            filter.put("propertyCode", propertyCode);
        if (hotelId != null)
            filter.put("hotelId", hotelId);
        if (hotelGroupCode != null)
            filter.put("hotelGroupCode", hotelGroupCode);

        // metricQueryService expects internal key; MetricQueryServiceImpl handles
        // normalization
        MetricResult result = metricQueryService.queryMetric(metric, periodValue, filter);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}