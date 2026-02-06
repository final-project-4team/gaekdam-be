package com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationPackageResponse {

    private Long packageCode;
    private String packageName;
    private String packageContent;
    private BigDecimal packagePrice;

    private Long propertyCode;
    private String propertyName;

    private LocalDateTime createdAt;
}
