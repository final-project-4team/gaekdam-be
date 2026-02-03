package com.gaekdam.gaekdambe.reservation_service.stay.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.reservation_service.stay.query.dto.request.CheckInOutSearchRequest;
import com.gaekdam.gaekdambe.reservation_service.stay.query.dto.response.CheckInOutResponse;
import com.gaekdam.gaekdambe.reservation_service.stay.query.service.CheckInOutQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "투숙")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/checkinouts")
public class CheckInOutQueryController {

    private final CheckInOutQueryService checkInOutQueryService;

    @GetMapping
    @PreAuthorize("hasAuthority('TODAY_RESERVATION_LIST')")
    @Operation(summary = "체크인/아웃 내역 리스트 조회", description = "체크인 및 체크아웃 내역을 리스트 조회합니다.")
    public ApiResponse<PageResponse<CheckInOutResponse>> getCheckInOuts(
            PageRequest page,
            CheckInOutSearchRequest search,
            SortRequest sort) {

        if (sort == null || sort.getSortBy() == null) {
            sort = new SortRequest();
            sort.setSortBy("recorded_at");
            sort.setDirection("DESC");
        }

        PageResponse<CheckInOutResponse> result = checkInOutQueryService.getCheckInOuts(page, search, sort);

        return ApiResponse.success(result);
    }
}
