package com.gaekdam.gaekdambe.operation_service.facility.query.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FacilitySearchRequest {

    private Long hotelGroupCode;

    // 검색 조건
    private Long propertyCode;
    private String keyword;        // facility_name
    private String facilityType;   // 식사 / 운동 / 휴식 / 여가 / 레저
    private String operatingStatus; // ACTIVE / INACTIVE
}
