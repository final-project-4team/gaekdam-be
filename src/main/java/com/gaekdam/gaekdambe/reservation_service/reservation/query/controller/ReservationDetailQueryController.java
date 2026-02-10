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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

@Tag(name = "예약")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reservations")
public class ReservationDetailQueryController {

    private final ReservationDetailQueryService reservationDetailQueryService;

    // 통합예약정보 상세보기
    @GetMapping("/detail/{reservationCode}")
    @PreAuthorize("hasAnyAuthority('RESERVATION_READ','CUSTOMER_READ')")
    @Operation(summary = "예약 상세 조회", description = "예약 코드를 사용하여 예약 상세 정보를 조회합니다.")
    public ApiResponse<ReservationDetailResponse> getReservationDetail(
            @Parameter(description = "예약 코드") @PathVariable Long reservationCode,
            @Parameter(description = "조회 사유") @RequestParam(required = false) String reason) {
        return ApiResponse.success(
                reservationDetailQueryService.getReservationDetail(reservationCode, reason));
    }
}
