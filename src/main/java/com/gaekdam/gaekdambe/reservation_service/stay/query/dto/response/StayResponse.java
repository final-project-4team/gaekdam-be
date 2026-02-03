package com.gaekdam.gaekdambe.reservation_service.stay.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StayResponse {

    private Long stayCode;
    private String stayStatus;

    private LocalDateTime actualCheckinAt;
    private LocalDateTime actualCheckoutAt;

    private int guestCount;

    private Long reservationCode;
    private Long roomCode;
    private Long customerCode;

    private LocalDateTime createdAt;
}
