package com.gaekdam.gaekdambe.customer_service.insight.query.controller;

import com.gaekdam.gaekdambe.customer_service.insight.query.dto.response.CustomerInsightResponse;
import com.gaekdam.gaekdambe.customer_service.insight.query.service.CustomerInsightService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "고객 분석")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers")
public class CustomerInsightController {

    private final CustomerInsightService customerInsightService;

    /**
     * 고객 분석 리포트 조회
     */
    @GetMapping("/{customerCode}/analysis-report")
    @Operation(summary = "고객 분석 리포트 조회", description = "특정 고객의 소비 패턴, KPI, 마케팅 인사이트(선호 객실, 부대시설 등)를 포함한 분석 리포트를 조회합니다.")
    // @PreAuthorize("hasAuthority('CUSTOMER_READ')") // 권한 검토 필요 시 추가
    public ApiResponse<CustomerInsightResponse> getCustomerInsight(
            @Parameter(description = "고객 코드") @PathVariable Long customerCode) {
        return ApiResponse.success(customerInsightService.getInsight(customerCode));
    }
}
