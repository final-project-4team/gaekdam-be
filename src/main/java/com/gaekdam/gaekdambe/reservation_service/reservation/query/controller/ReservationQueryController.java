package com.gaekdam.gaekdambe.reservation_service.reservation.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.request.OperationBoardSearchRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.request.ReservationSearchRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.OperationBoardResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.ReservationResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.TodayReservationSummaryResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.service.OperationBoardQueryService;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.service.ReservationQueryService;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.service.ReservationSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reservations")
public class ReservationQueryController {

    private final ReservationQueryService reservationQueryService;
    private final ReservationSummaryService reservationSummaryService;
    private final OperationBoardQueryService operationBoardQueryService;

    @GetMapping()
    public ApiResponse<PageResponse<ReservationResponse>> getReservations(
            @AuthenticationPrincipal CustomUser customUser,
            PageRequest page,
            ReservationSearchRequest search,
            SortRequest sort
    ) {

        Long hotelGroupCode = customUser.getHotelGroupCode();
        // SaaS 호텔 스코프 강제
        search.setHotelGroupCode(hotelGroupCode);

        if (sort == null || sort.getSortBy() == null) {
            sort = new SortRequest();
            sort.setSortBy("created_at");
            sort.setDirection("DESC");
        }

        return ApiResponse.success(
                reservationQueryService.getReservations(page, search, sort)
        );
    }




    @GetMapping("/today/summary")
    public ApiResponse<TodayReservationSummaryResponse> getTodayReservationSummary(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestParam(required = false) Long propertyCode
    ) {
        return ApiResponse.success(
                reservationSummaryService.getTodaySummary(customUser.getHotelGroupCode(), propertyCode)
        );
    }


    @GetMapping("/operations")
    public ApiResponse<PageResponse<OperationBoardResponse>> getOperationBoard(
            @AuthenticationPrincipal CustomUser customUser,
            PageRequest page,
            OperationBoardSearchRequest search,
            SortRequest sort
    ) {
        // SaaS 호텔 그룹 스코프 강제
        search.setHotelGroupCode(customUser.getHotelGroupCode());

        // 기본 정렬
        if (sort == null || sort.getSortBy() == null) {
            sort = new SortRequest();
            sort.setSortBy("t.plannedCheckinDate");
            sort.setDirection("DESC");
        }

        return ApiResponse.success(
                operationBoardQueryService.findOperationBoard(page, search, sort)
        );
    }

}
