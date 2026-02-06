package com.gaekdam.gaekdambe.operation_service.facility.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.crypto.HexUtils;
import com.gaekdam.gaekdambe.global.crypto.SearchHashService;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.AuditLog;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import com.gaekdam.gaekdambe.operation_service.facility.query.dto.request.FacilityUsageSearchRequest;
import com.gaekdam.gaekdambe.operation_service.facility.query.dto.response.FacilityUsageResponse;
import com.gaekdam.gaekdambe.operation_service.facility.query.dto.response.FacilityUsageSummaryResponse;
import com.gaekdam.gaekdambe.operation_service.facility.query.service.FacilityUsageQueryService;
import com.gaekdam.gaekdambe.operation_service.facility.query.service.FacilityUsageSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "부대시설")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/facility-usages")
public class FacilityUsageQueryController {

    private final FacilityUsageQueryService facilityUsageQueryService;
    private final FacilityUsageSummaryService facilityUsageSummaryService;
    private final SearchHashService searchHashService; //

    /**
     * 부대시설 이용내역 조회 (검색 + 페이징)
     */
    @GetMapping
    @PreAuthorize("hasAuthority('TODAY_FACILITY_USAGE_LIST')")
    @AuditLog(details = "", type = PermissionTypeKey.TODAY_FACILITY_USAGE_LIST)
    @Operation(summary = "부대시설 이용내역 조회", description = "부대시설 이용내역을 검색 및 페이징하여 조회합니다.")
    public ApiResponse<PageResponse<FacilityUsageResponse>> getFacilityUsages(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUser customUser,
            PageRequest page,
            FacilityUsageSearchRequest search,
            SortRequest sort,
            @Parameter(description = "고객명") @RequestParam(required = false) String customerName,
            @Parameter(description = "투숙 코드") @RequestParam(required = false) String stayCode) {

        /*
         * =========================
         * SaaS 스코프 주입
         * =========================
         */
        search.setHotelGroupCode(customUser.getHotelGroupCode());

        if (customerName != null && !customerName.isBlank()) {
            String hashHex = HexUtils.toHex(
                    searchHashService.nameHash(customerName));
            search.setCustomerNameHash(hashHex);
        }

        if (stayCode != null && !stayCode.isBlank()) {
            search.setStayCodeLike(stayCode);
        }

        /*
         * =========================
         * 기본 정렬
         * =========================
         */
        if (sort == null || sort.getSortBy() == null) {
            sort = new SortRequest();
            sort.setSortBy("usage_at");
            sort.setDirection("DESC");
        }

        PageResponse<FacilityUsageResponse> result = facilityUsageQueryService.getFacilityUsages(page, search, sort);

        return ApiResponse.success(result);
    }

    /**
     * 오늘 부대시설 이용 현황 (카드/요약)
     */
    @GetMapping("/today/summary")
    @PreAuthorize("hasAuthority('TODAY_FACILITY_USAGE_LIST')")
    @Operation(summary = "오늘 부대시설 이용 현황 요약", description = "오늘의 부대시설 이용 현황을 조회합니다.")
    public ApiResponse<List<FacilityUsageSummaryResponse>> getTodayFacilityUsageSummary(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUser customUser,
            @Parameter(description = "지점 코드") @RequestParam(required = false) Long propertyCode) {
        return ApiResponse.success(
                facilityUsageSummaryService.getTodaySummary(
                        LocalDate.now(),
                        customUser.getHotelGroupCode(),
                        propertyCode));
    }
}
