package com.gaekdam.gaekdambe.operation_service.facility.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FacilityResponse {

    private Long facilityCode;
    private String facilityName;
    private String facilityType;
    private String operatingHours;
    private String operatingStatus;

    private Long propertyCode;
    private String propertyName;

    private LocalDateTime createdAt;
}
