package com.gaekdam.gaekdambe.communication_service.inquiry.query.controller;

import com.gaekdam.gaekdambe.communication_service.inquiry.query.dto.request.InquiryListSearchRequest;
import com.gaekdam.gaekdambe.communication_service.inquiry.query.dto.response.InquiryDetailResponse;
import com.gaekdam.gaekdambe.communication_service.inquiry.query.dto.response.InquiryListResponse;
import com.gaekdam.gaekdambe.communication_service.inquiry.query.service.InquiryQueryService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inquiries")
public class InquiryQueryController {

    private final InquiryQueryService inquiryQueryService;

    @GetMapping
    public ApiResponse<PageResponse<InquiryListResponse>> getInquiries(
            @AuthenticationPrincipal CustomUser user,
            PageRequest page,
            InquiryListSearchRequest search,
            SortRequest sort
    ) {
        // 호텔그룹 스코프 강제 (Property 조인으로 필터링됨)
        search.setHotelGroupCode(user.getHotelGroupCode());

        if (sort == null || sort.getSortBy() == null) {
            sort = new SortRequest();
            sort.setSortBy("created_at");
            sort.setDirection("DESC");
        }

        return ApiResponse.success(
                inquiryQueryService.getInquiries(page, search, sort)
        );
    }

    @GetMapping("/{inquiryCode}")
    public ApiResponse<InquiryDetailResponse> getInquiryDetail(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long inquiryCode
    ) {
        return ApiResponse.success(
                inquiryQueryService.getInquiryDetail(user.getHotelGroupCode(), inquiryCode)
        );
    }
}
