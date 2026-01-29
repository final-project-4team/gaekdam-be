package com.gaekdam.gaekdambe.reservation_service.reservation.query.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.detail.ReservationDetailResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.service.ReservationDetailQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reservations")
public class ReservationDetailQueryController {

    private final ReservationDetailQueryService reservationDetailQueryService;

    // 통합예약정보 상세보기
    @GetMapping("/detail/{reservationCode}")
    @PreAuthorize("hasAuthority('RESERVATION_READ')")
    public ApiResponse<ReservationDetailResponse> getReservationDetail(
            @PathVariable Long reservationCode,
            @RequestParam(required = false) String reason) {
        return ApiResponse.success(
                reservationDetailQueryService.getReservationDetail(reservationCode, reason));
    }
}
