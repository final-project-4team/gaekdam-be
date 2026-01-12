package com.gaekdam.gaekdambe.operation_service.facility.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FacilityUsageSummaryResponse {

    private Long facilityCode;
    private String facilityName;
    private Long usageCount;

}
