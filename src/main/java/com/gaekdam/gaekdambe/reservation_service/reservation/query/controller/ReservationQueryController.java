package com.gaekdam.gaekdambe.reservation_service.reservation.query.controller;

import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.ReservationResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.service.ReservationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reservations")
public class ReservationQueryController {

    private final ReservationQueryService reservationQueryService;

    @GetMapping
    public List<ReservationResponse> getAllReservations() {
        return reservationQueryService.getAllReservations();
    }
}
