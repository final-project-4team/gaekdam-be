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
import com.gaekdam.gaekdambe.reservation_service.reservation.query.service.OperationBoardQueryService;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.service.ReservationQueryService;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.service.TodayOperationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reservations")
public class ReservationQueryController {

    private final OperationBoardQueryService operationBoardQueryService;
    private final TodayOperationQueryService todayOperationQueryService;
    private final ReservationQueryService reservationQueryService;


    @GetMapping
    public ApiResponse<PageResponse<ReservationResponse>> getReservations(
            @AuthenticationPrincipal CustomUser customUser,
            PageRequest page,
            ReservationSearchRequest search,
            SortRequest sort
    ) {
        search.setHotelGroupCode(customUser.getHotelGroupCode());

        if (sort == null || sort.getSortBy() == null) {
            sort = new SortRequest();
            sort.setSortBy("created_at");
            sort.setDirection("DESC");
        }

        return ApiResponse.success(
                reservationQueryService.getReservations(page, search, sort)
        );
    }


    // 통합 예약 조회 (리스트)
    @GetMapping("/operations")
    public ApiResponse<PageResponse<OperationBoardResponse>> getOperationBoard(
            @AuthenticationPrincipal CustomUser customUser,
            PageRequest page,
            OperationBoardSearchRequest search,
            SortRequest sort
    ) {
        search.setHotelGroupCode(customUser.getHotelGroupCode());

        if (sort == null || sort.getSortBy() == null) {
            sort = new SortRequest();
            sort.setSortBy("t.reservationCode");
            sort.setDirection("DESC");
        }

        return ApiResponse.success(
                operationBoardQueryService.findOperationBoard(page, search, sort)
        );
    }


    // 오늘의 예약정보 리스트(체크인예정 ,체크아웃예정, 투숙중)
    @GetMapping("/today/operations")
    public ApiResponse<PageResponse<OperationBoardResponse>> getTodayOperations(
            @AuthenticationPrincipal CustomUser customUser,
            PageRequest page,
            SortRequest sort,
            @RequestParam(required = false) String summaryType,
            @RequestParam(required = false) Long propertyCode,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String reservationCode
    ) {

        if (sort == null || sort.getSortBy() == null) {
            sort = new SortRequest();
            sort.setSortBy("t.plannedCheckoutDate");
            sort.setDirection("DESC");
        }

        return ApiResponse.success(
                todayOperationQueryService.findTodayOperations(
                        page,
                        customUser.getHotelGroupCode(),
                        propertyCode,
                        summaryType,
                        customerName,
                        reservationCode,
                        sort
                )
        );
    }


    // 오늘의 예약정보 카운트(체크인예정 ,체크아웃예정, 투숙중 상단 숫자카드)
    @GetMapping("/today/operations/summary")
    public ApiResponse<Map<String, Long>> getTodayOperationSummary(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestParam(required = false) Long propertyCode
    ) {
        return ApiResponse.success(
                todayOperationQueryService.getTodayOperationSummary(
                        customUser.getHotelGroupCode(),
                        propertyCode
                )
        );
    }


}
