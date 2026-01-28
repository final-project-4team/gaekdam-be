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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/facility-usages")
public class FacilityUsageQueryController {

    private final FacilityUsageQueryService facilityUsageQueryService;
    private final FacilityUsageSummaryService facilityUsageSummaryService;
    private final SearchHashService searchHashService; // 🔥 추가

    /**
     * 부대시설 이용내역 조회 (검색 + 페이징)
     */
    @GetMapping
    @AuditLog(details = "", type = PermissionTypeKey.TODAY_FACILITY_USAGE_LIST)
    public ApiResponse<PageResponse<FacilityUsageResponse>> getFacilityUsages(
            @AuthenticationPrincipal CustomUser customUser,
            PageRequest page,
            FacilityUsageSearchRequest search,
            SortRequest sort,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String stayCode
    ) {

        /* =========================
           SaaS 스코프 주입
           ========================= */
        search.setHotelGroupCode(customUser.getHotelGroupCode());


        if (customerName != null && !customerName.isBlank()) {
            String hashHex = HexUtils.toHex(
                    searchHashService.nameHash(customerName)
            );
            search.setCustomerNameHash(hashHex);
        }


        if (stayCode != null && !stayCode.isBlank()) {
            search.setStayCodeLike(stayCode);
        }

        /* =========================
           기본 정렬
           ========================= */
        if (sort == null || sort.getSortBy() == null) {
            sort = new SortRequest();
            sort.setSortBy("usage_at");
            sort.setDirection("DESC");
        }

        PageResponse<FacilityUsageResponse> result =
                facilityUsageQueryService.getFacilityUsages(page, search, sort);

        return ApiResponse.success(result);
    }

    /**
     * 오늘 부대시설 이용 현황 (카드/요약)
     */
    @GetMapping("/today/summary")
    public ApiResponse<List<FacilityUsageSummaryResponse>> getTodayFacilityUsageSummary(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestParam(required = false) Long propertyCode
    ) {
        return ApiResponse.success(
                facilityUsageSummaryService.getTodaySummary(
                        LocalDate.now(),
                        customUser.getHotelGroupCode(),
                        propertyCode
                )
        );
    }
}
