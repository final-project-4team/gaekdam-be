package com.gaekdam.gaekdambe.reservation_service.stay.query.dto.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CheckInOutSearchRequest {

    private Long stayCode;

    private String recordType;      // CHECK_IN / CHECK_OUT
    private String recordChannel;   // FRONT / KIOSK / MOBILE
    private String settlementYn;    // Y / N

    private LocalDate fromDate;
    private LocalDate toDate;
}