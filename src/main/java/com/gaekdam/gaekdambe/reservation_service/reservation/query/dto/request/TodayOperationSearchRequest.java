package com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodayOperationSearchRequest {

    private Long hotelGroupCode;
    private Long propertyCode;
    private String summaryType;

    // 검색 입력값
    private String customerName;        // 입력
    private String customerNameHash;    // 해시
    private String reservationCode;     // LIKE용 (String)
}
