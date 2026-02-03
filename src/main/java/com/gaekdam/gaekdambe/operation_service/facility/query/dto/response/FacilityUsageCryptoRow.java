package com.gaekdam.gaekdambe.operation_service.facility.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FacilityUsageCryptoRow {

    private Long facilityUsageCode;
    private LocalDateTime usageAt;

    private String usageType;
    private Integer usedPersonCount;
    private Integer usageQuantity;

    private BigDecimal usagePrice;
    private String priceSource;

    private Long stayCode;

    // customer crypto
    private Long customerCode;
    private byte[] customerNameEnc;   // DB 타입이 VARBINARY면 byte[]가 안전
    private String customerNameHash;
    private byte[] dekEnc;

    // room / facility
    private String roomNumber;

    private Long facilityCode;
    private String facilityName;
    private String facilityType;
}
