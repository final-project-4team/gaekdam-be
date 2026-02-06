package com.gaekdam.gaekdambe.reservation_service.reservation.command.application.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.application.dto.request.ReservationCreateRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.application.service.ReservationCreateCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "예약")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reservations")
public class ReservationCreateCommandController {

    private final ReservationCreateCommandService service;

    /**
     * 예약 등록 (등록 = 확정)
     */
    @PostMapping
    @Operation(summary = "예약 생성", description = "새로운 예약을 생성 합니다.")
    public ApiResponse<Long> create(
            @RequestBody ReservationCreateRequest request) {
        Long reservationCode = service.create(request);
        return ApiResponse.success(reservationCode);
    }
}
