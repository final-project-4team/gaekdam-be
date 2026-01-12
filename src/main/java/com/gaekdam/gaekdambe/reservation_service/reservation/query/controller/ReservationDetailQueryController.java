package com.gaekdam.gaekdambe.reservation_service.reservation.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.detail.ReservationDetailResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.service.ReservationDetailQueryService;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.service.ReservationSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reservations")
public class ReservationDetailQueryController {

    private final ReservationDetailQueryService reservationDetailQueryService;

    @GetMapping("/detail/{reservationCode}")
    public ApiResponse<ReservationDetailResponse> getReservationDetail(
            @PathVariable Long reservationCode
    ) {
        return ApiResponse.success(
                reservationDetailQueryService.getReservationDetail(reservationCode)
        );
    }
}
