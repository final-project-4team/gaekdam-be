package com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReservationPackageSearchRequest {

    private Long hotelGroupCode;

    // 검색 조건
    private Long propertyCode;
    private String keyword; // package_name 검색
}
