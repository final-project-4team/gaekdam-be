package com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TodayReservationSummaryResponse {

    private long totalCount;            // ALL
    private long todayCheckInCount;     // 체크인 예정
    private long todayCheckOutCount;    // 체크아웃 예정
    private long stayingRoomCount;      // 현재 투숙 객실

}
