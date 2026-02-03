package com.gaekdam.gaekdambe.operation_service.facility.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.operation_service.facility.query.dto.request.FacilitySearchRequest;
import com.gaekdam.gaekdambe.operation_service.facility.query.dto.response.FacilityResponse;
import com.gaekdam.gaekdambe.operation_service.facility.query.service.FacilityQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "부대시설")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/facilities")
public class FacilityQueryController {

    private final FacilityQueryService service;

    @GetMapping()
    @Operation(summary = "부대시설 목록 조회", description = "호텔 그룹에 속한 부대시설 목록을 조회합니다.")
    public ApiResponse<PageResponse<FacilityResponse>> getFacilities(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUser customUser,
            PageRequest page,
            FacilitySearchRequest search,
            SortRequest sort) {
        search.setHotelGroupCode(customUser.getHotelGroupCode());

        if (sort == null || sort.getSortBy() == null) {
            sort = new SortRequest();
            sort.setSortBy("f.created_at");
            sort.setDirection("DESC");
        }

        return ApiResponse.success(
                service.getFacilities(page, search, sort));
    }
}
