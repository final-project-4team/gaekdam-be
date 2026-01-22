package com.gaekdam.gaekdambe.operation_service.facility.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.operation_service.facility.query.dto.request.FacilityUsageSearchRequest;
import com.gaekdam.gaekdambe.operation_service.facility.query.dto.response.FacilityUsageResponse;
import com.gaekdam.gaekdambe.operation_service.facility.query.dto.response.FacilityUsageSummaryResponse;
import com.gaekdam.gaekdambe.operation_service.facility.query.service.FacilityUsageQueryService;
import com.gaekdam.gaekdambe.operation_service.facility.query.service.FacilityUsageSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/facility-usages")
public class FacilityUsageQueryController {

    private final FacilityUsageQueryService facilityUsageQueryService;
    private final FacilityUsageSummaryService facilityUsageSummaryService;


    // 현재 로그인 정보를 못불러오니
    // 일단 파라미터값으로 호텔그룹코드를 받아온다
    // 추후 유저 생기면 로그인 객체로 받아서 사용 -> 변경 완료

    /**
     * 부대시설 이용내역 조회 (검색 + 페이징)
     */
    @GetMapping()
    public ApiResponse<PageResponse<FacilityUsageResponse>> getFacilityUsages(
            @AuthenticationPrincipal CustomUser customUser,
            PageRequest page,
            FacilityUsageSearchRequest search,
            SortRequest sort
    ) {

        // SaaS 호텔 스코프 주입
        search.setHotelGroupCode(customUser.getHotelGroupCode());

        // 도메인 기본 정렬
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


