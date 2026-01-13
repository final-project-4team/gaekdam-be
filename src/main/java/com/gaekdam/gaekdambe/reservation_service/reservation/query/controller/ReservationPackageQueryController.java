package com.gaekdam.gaekdambe.reservation_service.reservation.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.request.ReservationPackageSearchRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.ReservationPackageResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.service.ReservationPackageQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reservation-packages")
public class ReservationPackageQueryController {

    private final ReservationPackageQueryService service;

    @GetMapping("/{hotelGroupCode}")
    public ApiResponse<PageResponse<ReservationPackageResponse>> getPackages(
            @PathVariable Long hotelGroupCode,
            PageRequest page,
            ReservationPackageSearchRequest search,
            SortRequest sort
    ) {

        search.setHotelGroupCode(hotelGroupCode);

        if (sort == null || sort.getSortBy() == null) {
            sort = new SortRequest();
            sort.setSortBy("rp.created_at");
            sort.setDirection("DESC");
        }

        return ApiResponse.success(
                service.getPackages(page, search, sort)
        );
    }
}
