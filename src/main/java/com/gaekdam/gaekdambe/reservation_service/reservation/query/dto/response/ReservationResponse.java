package com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {

    private Long reservationCode;
    private String reservationStatus;

    private LocalDate checkinDate;
    private LocalDate checkoutDate;

    private Integer guestCount;
    private String guestType;
    private String reservationChannel;

    // 금액
    private BigDecimal reservationRoomPrice;
    private BigDecimal reservationPackagePrice;
    private BigDecimal totalPrice;

    // 시간
    private LocalDateTime reservedAt;
    private LocalDateTime canceledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 연관 코드
    private Long propertyCode;
    private Long roomCode;
    private Long customerCode;
    private Long packageCode;

}
