package com.gaekdam.gaekdambe.reservation_service.timeline.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response.CustomerStayResponse;
import com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response.TimelineDetailResponse;
import com.gaekdam.gaekdambe.reservation_service.timeline.query.service.TimelineQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/timeline")
public class TimelineQueryController {

    private final TimelineQueryService timelineQueryService;

    // 고객 선택 → 투숙 리스트
    @GetMapping("/customers/{customerCode}/stays")
    public ApiResponse<List<CustomerStayResponse>> getCustomerStays(
            @PathVariable Long customerCode
    ) {
        return ApiResponse.success(
                timelineQueryService.getCustomerStays(customerCode)
        );
    }

    // 투숙 선택 → 타임라인
    @GetMapping("/stays/{stayCode}")
    public ApiResponse<TimelineDetailResponse> getTimeline(
            @PathVariable Long stayCode
    ) {
        return ApiResponse.success(
                timelineQueryService.getTimeline(stayCode)
        );
    }
}
