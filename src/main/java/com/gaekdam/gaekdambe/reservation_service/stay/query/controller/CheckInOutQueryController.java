package com.gaekdam.gaekdambe.reservation_service.stay.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.reservation_service.stay.query.dto.request.CheckInOutSearchRequest;
import com.gaekdam.gaekdambe.reservation_service.stay.query.dto.response.CheckInOutResponse;
import com.gaekdam.gaekdambe.reservation_service.stay.query.service.CheckInOutQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/checkinouts")
public class CheckInOutQueryController {

    private final CheckInOutQueryService checkInOutQueryService;

    @GetMapping
    @PreAuthorize("hasAuthority('TODAY_RESERVATION_LIST')")
    public ApiResponse<PageResponse<CheckInOutResponse>> getCheckInOuts(
            PageRequest page,
            CheckInOutSearchRequest search,
            SortRequest sort
    ) {

        if (sort == null || sort.getSortBy() == null) {
            sort = new SortRequest();
            sort.setSortBy("recorded_at");
            sort.setDirection("DESC");
        }

        PageResponse<CheckInOutResponse> result = checkInOutQueryService.getCheckInOuts(page, search, sort);

        return ApiResponse.success(result);
    }
}
