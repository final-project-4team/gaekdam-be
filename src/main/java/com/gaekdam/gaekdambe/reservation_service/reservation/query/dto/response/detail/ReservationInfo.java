package com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.detail;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ReservationInfo {
    private Long reservationCode;
    private String reservationStatus;
    private String reservationChannel;
    private LocalDate checkinDate;
    private LocalDate checkoutDate;
    private int guestCount;
    private String guestType;
    private String requestNote;
    /* ===== 금액 정보 ===== */
    private BigDecimal reservationRoomPrice;     // 방 비용
    private BigDecimal reservationPackagePrice;  // 패키지 비용
    private BigDecimal totalPrice;               // 총 금액

    private LocalDateTime createdAt;
}