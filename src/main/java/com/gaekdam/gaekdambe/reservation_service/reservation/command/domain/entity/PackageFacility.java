package com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "package_facility")
public class PackageFacility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "package_facility_code")
    private Long packageFacilityCode;

    @Column(name = "package_code", nullable = false)
    private Long packageCode;

    @Column(name = "facility_code", nullable = false)
    private Long facilityCode;

    @Column(name = "included_quantity")
    private Integer includedQuantity;

    @Column(name = "included_price", precision = 10, scale = 2)
    private BigDecimal includedPrice;

    public static PackageFacility createPackageFacility(
            Long packageCode,
            Long facilityCode,
            Integer quantity,
            BigDecimal price
    ) {
        return PackageFacility.builder()
                .packageCode(packageCode)
                .facilityCode(facilityCode)
                .includedQuantity(quantity)
                .includedPrice(price)
                .build();
    }
}
