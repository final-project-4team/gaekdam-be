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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "타임라인")
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
        @Operation(summary = "타임라인 고객 검색", description = "투숙 정보를 기반으로 고객을 검색합니다.")
        public ApiResponse<List<TimelineCustomerResponse>> getTimelineCustomers(
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUser principal,
                        @Parameter(description = "검색 키워드 (고객명, 연락처 등)") @RequestParam(required = false) String keyword) {
                return ApiResponse.success(
                                timelineCustomerQueryService.findTimelineCustomers(
                                                principal.getHotelGroupCode(),
                                                keyword));
        }

        /**
         * 고객 선택 → 투숙 리스트
         */
        @GetMapping("/customers/{customerCode}/stays")
        @PreAuthorize("hasAuthority('CUSTOMER_TIMELINE_READ')")
        @AuditLog(details = "", type = PermissionTypeKey.CUSTOMER_TIMELINE_READ)
        @Operation(summary = "고객별 투숙 리스트 조회", description = "특정 고객의 과거 및 현재 투숙 리스트를 조회합니다.")
        public ApiResponse<List<CustomerStayResponse>> getCustomerStays(
                        @Parameter(description = "고객 코드") @PathVariable Long customerCode,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUser principal) {
                return ApiResponse.success(
                                timelineQueryService.getCustomerStays(
                                                principal.getHotelGroupCode(),
                                                customerCode));
        }

        /**
         * 투숙 선택 → 타임라인
         */
        @GetMapping("/stays/{stayCode}")
        @PreAuthorize("hasAuthority('CUSTOMER_TIMELINE_READ')")
        @AuditLog(details = "", type = PermissionTypeKey.CUSTOMER_TIMELINE_READ)
        @Operation(summary = "투숙 타임라인 상세 조회", description = "특정 투숙의 전체 타임라인을 상세 조회합니다.")
        public ApiResponse<TimelineDetailResponse> getTimeline(
                        @Parameter(description = "투숙 코드") @PathVariable Long stayCode,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUser principal) {
                return ApiResponse.success(
                                timelineQueryService.getTimeline(
                                                principal.getHotelGroupCode(),
                                                stayCode));
        }
}
