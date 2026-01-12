package com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationSearchRequest {

    private Long hotelGroupCode;

    // 공통
    private String keyword;              // 자유 검색
    private LocalDate fromDate;          // 예약일 시작
    private LocalDate toDate;            // 예약일 종료

    // 예약 도메인 전용
    private String status;               // RESERVED / CANCELED / NO_SHOW
    private String reservationChannel;   // WEB / OTA
    private String guestType;             // INDIVIDUAL / FAMILY

    private Long propertyCode;
    private Long roomCode;
    private Long customerCode;
    private Boolean hasPackage;           // 패키지 포함 여부
}
