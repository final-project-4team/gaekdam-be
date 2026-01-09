package com.gaekdam.gaekdambe.operation_service.facility.command.domain.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "facility_usage")
public class FacilityUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "facility_usage_code")
    private Long facilityUsageCode;

    @Column(name = "usage_at", nullable = false)
    private LocalDateTime usageAt;

    @Column(name = "usage_type", nullable = false, length = 20)
    private String usageType; // PERSONAL, WITH_GUEST

    @Column(name = "used_person_count")
    private Integer usedPersonCount;

    @Column(name = "usage_quantity")
    private Integer usageQuantity;

    @Column(name = "usage_price", nullable = false)
    private BigDecimal usagePrice;

    @Column(name = "price_source", length = 20)
    private String priceSource; // PACKAGE, EXTRA

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "stay_code", nullable = false)
    private Long stayCode;

    @Column(name = "facility_code", nullable = false)
    private Long facilityCode;



    public static FacilityUsage createPackageUsage(
            Long stayCode,
            Long facilityCode,
            int personCount
    ) {
        return FacilityUsage.builder()
                .stayCode(stayCode)
                .facilityCode(facilityCode)
                .usageAt(LocalDateTime.now())
                .usageType("PERSONAL")
                .usedPersonCount(personCount)
                .usagePrice(BigDecimal.ZERO)
                .priceSource("PACKAGE")
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static FacilityUsage createExtraUsage(
            Long stayCode,
            Long facilityCode,
            int quantity,
            BigDecimal price
    ) {
        return FacilityUsage.builder()
                .stayCode(stayCode)
                .facilityCode(facilityCode)
                .usageAt(LocalDateTime.now())
                .usageType("PERSONAL")
                .usageQuantity(quantity)
                .usagePrice(price)
                .priceSource("EXTRA")
                .createdAt(LocalDateTime.now())
                .build();
    }
}
