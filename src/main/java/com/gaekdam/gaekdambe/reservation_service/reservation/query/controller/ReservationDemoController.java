package com.gaekdam.gaekdambe.reservation_service.reservation.query.controller;

import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.DemoReservationResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.service.ReservationDemoQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/demo/reservations")
public class ReservationDemoController {

    private final ReservationDemoQueryService demoQueryService;

    @GetMapping("/one")
    public ResponseEntity<DemoReservationResponse> getOne(
            @AuthenticationPrincipal CustomUser loginUser
    ) {
        DemoReservationResponse res =
                demoQueryService.getDemoReservation(loginUser.getHotelGroupCode());

        if (res == null) {
            return ResponseEntity.noContent().build(); // 204
        }

        return ResponseEntity.ok(res);
    }
}
