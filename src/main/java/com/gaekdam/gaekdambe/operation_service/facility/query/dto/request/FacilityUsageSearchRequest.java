package com.gaekdam.gaekdambe.operation_service.facility.query.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class FacilityUsageSearchRequest {

    private Long stayCode;
    private Long facilityCode;

    private String usageType;     // PERSONAL / WITH_GUEST
    private String priceSource;   // PACKAGE / EXTRA

    private LocalDate fromDate;
    private LocalDate toDate;
}
