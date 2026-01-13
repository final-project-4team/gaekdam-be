package com.gaekdam.gaekdambe.operation_service.facility.command.domain.entity;

import com.gaekdam.gaekdambe.operation_service.facility.command.domain.enums.FacilityUsageType;
import com.gaekdam.gaekdambe.operation_service.facility.command.domain.enums.PriceSource;
import jakarta.persistence.*;
import lombok.*;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "usage_type", nullable = false, length = 20)
    private FacilityUsageType usageType;

    @Column(name = "used_person_count")
    private Integer usedPersonCount;

    @Column(name = "usage_quantity")
    private Integer usageQuantity;

    @Column(name = "usage_price", nullable = false)
    private BigDecimal usagePrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_source", nullable = false, length = 20)
    private PriceSource priceSource;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "stay_code", nullable = false)
    private Long stayCode;

    @Column(name = "facility_code", nullable = false)
    private Long facilityCode;

    /* =========================
       생성 메서드
       ========================= */

    // 패키지 포함 이용
    public static FacilityUsage createPackageUsage(
            Long stayCode,
            Long facilityCode,
            int personCount
    ) {
        LocalDateTime now = LocalDateTime.now();

        return FacilityUsage.builder()
                .stayCode(stayCode)
                .facilityCode(facilityCode)
                .usageAt(now)
                .usageType(FacilityUsageType.PERSONAL)
                .usedPersonCount(personCount)
                .usagePrice(BigDecimal.ZERO)
                .priceSource(PriceSource.PACKAGE)
                .createdAt(now)
                .build();
    }

    // 추가 결제 이용
    public static FacilityUsage createExtraUsage(
            Long stayCode,
            Long facilityCode,
            int quantity,
            BigDecimal price
    ) {
        LocalDateTime now = LocalDateTime.now();

        return FacilityUsage.builder()
                .stayCode(stayCode)
                .facilityCode(facilityCode)
                .usageAt(now)
                .usageType(FacilityUsageType.PERSONAL)
                .usageQuantity(quantity)
                .usagePrice(price)
                .priceSource(PriceSource.EXTRA)
                .createdAt(now)
                .build();
    }
}
