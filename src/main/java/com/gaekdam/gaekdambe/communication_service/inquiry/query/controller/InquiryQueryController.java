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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@Tag(name="문의 조회")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inquiries")
public class InquiryQueryController {

    private final InquiryQueryService inquiryQueryService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('INQUIRY_LIST','CUSTOMER_READ')")
    @AuditLog(details = "", type = PermissionTypeKey.INQUIRY_LIST)
    @Operation(summary = "문의 리스트 조회", description = "호텔에 대한 문의 리스트를 조회한다.")
    public ApiResponse<PageResponse<InquiryListResponse>> getInquiries(
            @AuthenticationPrincipal CustomUser user,
            @Parameter(description = "페이징 값")PageRequest page,
            @Parameter(description = "검색 키워드")InquiryListSearchRequest search,
            @Parameter(description = "정렬 기준")SortRequest sort
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
    @PreAuthorize("hasAnyAuthority('INQUIRY_READ','CUSTOMER_READ')")
    @AuditLog(details = "", type = PermissionTypeKey.INQUIRY_READ)
    @Operation(summary = "문의 상세 조회", description = "특정 문의에 대해 상세 조회 한다.")
    public ApiResponse<InquiryDetailResponse> getInquiryDetail(
            @AuthenticationPrincipal CustomUser user,
            @Parameter(description = "문의 코드")@PathVariable Long inquiryCode
    ) {
        return ApiResponse.success(
                inquiryQueryService.getInquiryDetail(user.getHotelGroupCode(), inquiryCode)
        );
    }
}
