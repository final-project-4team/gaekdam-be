package com.gaekdam.gaekdambe.operation_service.facility.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.operation_service.facility.query.dto.request.FacilityUsageSearchRequest;
import com.gaekdam.gaekdambe.operation_service.facility.query.dto.response.FacilityUsageResponse;
import com.gaekdam.gaekdambe.operation_service.facility.query.service.FacilityUsageQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/facility-usages")
public class FacilityUsageQueryController {

    private final FacilityUsageQueryService facilityUsageQueryService;

    @GetMapping
    public ApiResponse<PageResponse<FacilityUsageResponse>> getFacilityUsages(
            PageRequest page,
            FacilityUsageSearchRequest search,
            SortRequest sort
    ) {
        // 도메인 기본 정렬
        if (sort == null || sort.getSortBy() == null) {
            sort = new SortRequest();
            sort.setSortBy("usage_at");
            sort.setDirection("DESC");
        }

        PageResponse<FacilityUsageResponse> result = facilityUsageQueryService.getFacilityUsages(page, search, sort);
        return ApiResponse.success(result);
    }
}
