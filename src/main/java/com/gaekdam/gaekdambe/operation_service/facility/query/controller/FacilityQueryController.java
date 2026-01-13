package com.gaekdam.gaekdambe.operation_service.facility.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.operation_service.facility.query.dto.request.FacilitySearchRequest;
import com.gaekdam.gaekdambe.operation_service.facility.query.dto.response.FacilityResponse;
import com.gaekdam.gaekdambe.operation_service.facility.query.service.FacilityQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/facilities")
public class FacilityQueryController {

    private final FacilityQueryService service;

    @GetMapping("/{hotelGroupCode}")
    public ApiResponse<PageResponse<FacilityResponse>> getFacilities(
            @PathVariable Long hotelGroupCode,
            PageRequest page,
            FacilitySearchRequest search,
            SortRequest sort
    ) {
        search.setHotelGroupCode(hotelGroupCode);

        if (sort == null || sort.getSortBy() == null) {
            sort = new SortRequest();
            sort.setSortBy("f.created_at");
            sort.setDirection("DESC");
        }

        return ApiResponse.success(
                service.getFacilities(page, search, sort)
        );
    }
}
