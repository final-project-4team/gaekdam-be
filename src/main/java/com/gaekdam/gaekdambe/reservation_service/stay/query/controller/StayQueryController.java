package com.gaekdam.gaekdambe.reservation_service.stay.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.reservation_service.stay.query.dto.request.StaySearchRequest;
import com.gaekdam.gaekdambe.reservation_service.stay.query.dto.response.StayResponse;
import com.gaekdam.gaekdambe.reservation_service.stay.query.service.StayQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "투숙")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stays")
public class StayQueryController {

    private final StayQueryService stayQueryService;

    @GetMapping()
    @Operation(summary = "투숙 리스트 조회", description = "투숙 내역을 리스트 조회합니다.")
    public ApiResponse<PageResponse<StayResponse>> getStays(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUser customUser,
            PageRequest page,
            StaySearchRequest search,
            SortRequest sort) {

        search.setHotelGroupCode(customUser.getHotelGroupCode());

        if (sort == null || sort.getSortBy() == null) {
            sort = new SortRequest();
            sort.setSortBy("s.created_at");
        }

        PageResponse<StayResponse> result = stayQueryService.getStays(page, search, sort);

        return ApiResponse.success(result);
    }
}
