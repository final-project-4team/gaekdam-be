package com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {

    private Long reservationCode;
    private String reservationStatus;
    private LocalDate checkinDate;
    private LocalDate checkoutDate;
    private Integer guestCount;
    private String guestType;
    private String reservationChannel;
    private LocalDateTime reservedAt;
    private Long tenantCode;
    private Long roomCode;
    private Long customerId;

}
