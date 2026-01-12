package com.gaekdam.gaekdambe.operation_service.facility.query.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class FacilityUsageSearchRequest {

    private Long hotelGroupCode;
    private Long propertyCode;

    private Long stayCode;
    private Long facilityCode;

    private String usageType;     // PERSONAL / WITH_GUEST
    private String priceSource;   // PACKAGE / EXTRA

    private LocalDate fromDate;
    private LocalDate toDate;

    private LocalDate date; // 오늘날짜 (today집계용)
}
