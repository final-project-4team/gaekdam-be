package com.gaekdam.gaekdambe.operation_service.facility.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FacilityUsageResponse {

    private Long facilityUsageCode;
    private LocalDateTime usageAt;

    private String usageType;
    private Integer usedPersonCount;
    private Integer usageQuantity;

    private BigDecimal usagePrice;
    private String priceSource;

    private Long stayCode;
    private Long facilityCode;
}
