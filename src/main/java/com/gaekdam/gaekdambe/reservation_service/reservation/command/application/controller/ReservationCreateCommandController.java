package com.gaekdam.gaekdambe.reservation_service.reservation.command.application.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.application.dto.request.ReservationCreateRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.application.service.ReservationCreateCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reservations")
public class ReservationCreateCommandController {

    private final ReservationCreateCommandService service;

    /**
     * 예약 등록 (등록 = 확정)
     */
    @PostMapping
    public ApiResponse<Long> create(
            @RequestBody ReservationCreateRequest request
    ) {
        Long reservationCode = service.create(request);
        return ApiResponse.success(reservationCode);
    }
}
