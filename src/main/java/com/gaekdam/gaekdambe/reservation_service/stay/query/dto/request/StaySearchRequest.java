package com.gaekdam.gaekdambe.reservation_service.stay.query.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StaySearchRequest {

    private String stayStatus;  // STAYING / COMPLETED

    private Long reservationCode;
    private Long roomCode;
    private Long customerCode;

    private LocalDate fromDate;
    private LocalDate toDate;
}
