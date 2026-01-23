package com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.request;

import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.enums.OperationStatus;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.enums.OperationSummaryType;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.enums.ReservationStatus;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.enums.StayStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class OperationBoardSearchRequest {

    // SaaS 스코프
    private Long hotelGroupCode;

    // 필터
    private Long propertyCode;
    private String status;

    // 검색 / 상세검색
    private String keyword;
    private String customerName;     // 고객명 검색
    private String customerNameHash;
    private Long reservationCode;    // 예약번호 검색


    // summary 전용
    private OperationSummaryType summaryType;

    // 내부 변환용 (프론트에서는 안 씀)
    private LocalDate fromDate;
    private LocalDate toDate;

    private LocalDate checkinDate;
    private LocalDate checkoutDate;

    private ReservationStatus reservationStatus;
    private StayStatus stayStatus;
}