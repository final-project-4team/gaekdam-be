package com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DemoReservationResponse {
    private Long reservationCode;
    private String reservationStatus;
    private String phone; // 평문
}
