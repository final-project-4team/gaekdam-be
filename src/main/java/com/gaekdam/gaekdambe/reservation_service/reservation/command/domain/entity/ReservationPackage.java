package com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity;

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
@Table(name = "reservation_package")
public class ReservationPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "package_code")
    private Long packageCode;

    @Column(name = "package_name", nullable = false, length = 50)
    private String packageName;

    @Column(name = "package_content", nullable = false, length = 255)
    private String packageContent;

    @Column(name = "package_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal packagePrice;

    @Column(name = "property_code", nullable = false)
    private Long propertyCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static ReservationPackage createReservationPackage(
            String name,
            String content,
            BigDecimal price,
            Long propertyCode
    ) {
        LocalDateTime now = LocalDateTime.now();
        return ReservationPackage.builder()
                .packageName(name)
                .packageContent(content)
                .packagePrice(price)
                .propertyCode(propertyCode)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
