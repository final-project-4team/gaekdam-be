package com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.detail;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PackageFacilityInfo {
    private String facilityName;
    private Integer includedQuantity;
}
