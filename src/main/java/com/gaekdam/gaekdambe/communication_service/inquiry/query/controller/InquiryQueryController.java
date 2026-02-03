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
import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.AuditLog;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inquiries")
public class InquiryQueryController {

    private final InquiryQueryService inquiryQueryService;

    @GetMapping
    @PreAuthorize("hasAuthority('INQUIRY_LIST')")
    @AuditLog(details = "", type = PermissionTypeKey.INQUIRY_LIST)
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
    @PreAuthorize("hasAuthority('INQUIRY_READ')")
    @AuditLog(details = "", type = PermissionTypeKey.INQUIRY_READ)
    public ApiResponse<InquiryDetailResponse> getInquiryDetail(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long inquiryCode
    ) {
        return ApiResponse.success(
                inquiryQueryService.getInquiryDetail(user.getHotelGroupCode(), inquiryCode)
        );
    }
}
