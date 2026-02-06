package com.gaekdam.gaekdambe.reservation_service.stay.query.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StaySearchRequest {

    private Long hotelGroupCode;
    private Long propertyCode;

    private String stayStatus;  // STAYING / COMPLETED

    private Long reservationCode;
    private Long roomCode;
    private Long customerCode;

    private LocalDate fromDate;
    private LocalDate toDate;
}
