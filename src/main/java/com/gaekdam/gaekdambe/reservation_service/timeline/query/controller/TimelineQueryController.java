package com.gaekdam.gaekdambe.reservation_service.timeline.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.AuditLog;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response.CustomerStayResponse;
import com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response.TimelineCustomerResponse;
import com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response.TimelineDetailResponse;
import com.gaekdam.gaekdambe.reservation_service.timeline.query.service.TimelineCustomerQueryService;
import com.gaekdam.gaekdambe.reservation_service.timeline.query.service.TimelineQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/timeline")
public class TimelineQueryController {

    private final TimelineQueryService timelineQueryService;
    private final TimelineCustomerQueryService timelineCustomerQueryService;

    /**
     * 타임라인 고객 검색 (stay 기준)
     */
    @GetMapping("/customers")
    @PreAuthorize("hasAuthority('CUSTOMER_TIMELINE_READ')")
    @AuditLog(details = "", type = PermissionTypeKey.CUSTOMER_TIMELINE_READ)
    public ApiResponse<List<TimelineCustomerResponse>> getTimelineCustomers(
            @AuthenticationPrincipal CustomUser principal,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(
                timelineCustomerQueryService.findTimelineCustomers(
                        principal.getHotelGroupCode(),
                        keyword
                )
        );
    }

    /**
     * 고객 선택 → 투숙 리스트
     */
    @GetMapping("/customers/{customerCode}/stays")
    @PreAuthorize("hasAuthority('CUSTOMER_TIMELINE_READ')")
    @AuditLog(details = "", type = PermissionTypeKey.CUSTOMER_TIMELINE_READ)
    public ApiResponse<List<CustomerStayResponse>> getCustomerStays(
            @PathVariable Long customerCode,
            @AuthenticationPrincipal CustomUser principal
    ) {
        return ApiResponse.success(
                timelineQueryService.getCustomerStays(
                        principal.getHotelGroupCode(),
                        customerCode
                )
        );
    }

    /**
     * 투숙 선택 → 타임라인
     */
    @GetMapping("/stays/{stayCode}")
    @PreAuthorize("hasAuthority('CUSTOMER_TIMELINE_READ')")
    @AuditLog(details = "", type = PermissionTypeKey.CUSTOMER_TIMELINE_READ)
    public ApiResponse<TimelineDetailResponse> getTimeline(
            @PathVariable Long stayCode,
            @AuthenticationPrincipal CustomUser principal
    ) {
        return ApiResponse.success(
                timelineQueryService.getTimeline(
                        principal.getHotelGroupCode(),
                        stayCode
                )
        );
    }
}
