package com.gaekdam.gaekdambe.customer_service.customer.query.controller;

import com.gaekdam.gaekdambe.customer_service.customer.query.dto.request.CustomerListSearchRequest;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.request.CustomerStatusHistoryRequest;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.*;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.item.CustomerListItem;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.CustomerQueryService;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.CustomerSnapshotQueryService;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.CustomerTimelineQueryService;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers")
public class CustomerQueryController {

    private final CustomerQueryService customerQueryService;
    private final CustomerSnapshotQueryService snapshotQueryService;
    private final CustomerTimelineQueryService timelineQueryService;

    /**
     * 고객 목록 조회
     * - 상단 keyword + 상세검색(모달) + 필터
     */
    @GetMapping
    public ApiResponse<PageResponse<CustomerListItem>> getCustomerList(
            @AuthenticationPrincipal CustomUser user,
            @ModelAttribute CustomerListSearchRequest request
    ) {
        request.setHotelGroupCode(user.getHotelGroupCode());
        return ApiResponse.success(customerQueryService.getCustomerList(request));
    }

    /**
     * 고객 상세 조회
     */
    @GetMapping("/{customerCode}")
    public ApiResponse<CustomerDetailResponse> getCustomerDetail(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long customerCode
    ) {
        return ApiResponse.success(
                customerQueryService.getCustomerDetail(user.getHotelGroupCode(), customerCode)
        );
    }

    /**
     * 고객 상태 조회
     */
    @GetMapping("/{customerCode}/status")
    public ApiResponse<CustomerStatusResponse> getCustomerStatus(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long customerCode
    ) {
        return ApiResponse.success(
                customerQueryService.getCustomerStatus(user.getHotelGroupCode(), customerCode)
        );
    }

    /**
     * 고객 상태 변경 이력 조회 (paging)
     */
    @GetMapping("/{customerCode}/status-histories")
    public ApiResponse<CustomerStatusHistoryResponse> getCustomerStatusHistories(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long customerCode,
            @ModelAttribute CustomerStatusHistoryRequest request
    ) {
        return ApiResponse.success(
                customerQueryService.getCustomerStatusHistories(user.getHotelGroupCode(), customerCode, request)
        );
    }

    /**
     * 연락처별 마케팅 수신 동의 조회
     */
    @GetMapping("/{customerCode}/marketing-consents")
    public ApiResponse<CustomerMarketingConsentResponse> getCustomerMarketingConsents(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long customerCode
    ) {
        return ApiResponse.success(
                customerQueryService.getCustomerMarketingConsents(user.getHotelGroupCode(), customerCode)
        );
    }

    /**
     * 고객 스냅샷 조회
     * GET /api/v1/customers/{customerCode}/snapshot
     */
    @GetMapping("/{customerCode}/snapshot")
    public ApiResponse<CustomerSnapshotResponse> getCustomerSnapshot(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long customerCode
    ) {
        return ApiResponse.success(
                snapshotQueryService.getSnapshot(user.getHotelGroupCode(), customerCode)
        );
    }

    /**
     * 고객 타임라인 조회 (통합 이벤트 리스트)
     * GET /api/v1/customers/{customerCode}/timeline?limit=50
     */
    @GetMapping("/{customerCode}/timeline")
    public ApiResponse<CustomerTimelineResponse> getCustomerTimeline(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long customerCode,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return ApiResponse.success(
                timelineQueryService.getTimeline(user.getHotelGroupCode(), customerCode, limit)
        );
    }
}
