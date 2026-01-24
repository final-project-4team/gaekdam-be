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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/metric")
public class MetricTestController {

    private final MetricQueryService metricQueryService;

    @GetMapping
    public ResponseEntity<ApiResponse<MetricResult>> getMetric(
        @RequestParam String metric,                // CHECKIN, CHECKOUT, ADR, OCC_RATE 등
        @RequestParam(name = "period", required = true) String periodValue,                // YYYY or YYYY-MM
        @RequestParam(required = false) Long hotelId,
        @RequestParam(required = false, name = "hotel_group_code") Long hotelGroupCode
    ) {
        Map<String, Object> filter = new HashMap<>();
        if (hotelId != null) filter.put("hotelId", hotelId);
        if (hotelGroupCode != null) filter.put("hotelGroupCode", hotelGroupCode);

        // metricQueryService expects internal key; MetricQueryServiceImpl handles normalization
        MetricResult result = metricQueryService.queryMetric(metric, periodValue, filter);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}