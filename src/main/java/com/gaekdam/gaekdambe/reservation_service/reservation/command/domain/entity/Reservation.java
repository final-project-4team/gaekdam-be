package com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_code")
    private Long reservationCode;

    @Column(name = "reservation_status", nullable = false, length = 20)
    private String reservationStatus; // RESERVED, CANCELED, NO_SHOW

    @Column(name = "checkin_date", nullable = false)
    private LocalDate checkinDate;

    @Column(name = "checkout_date", nullable = false)
    private LocalDate checkoutDate;

    @Column(name = "guest_count", nullable = false)
    private int guestCount;

    @Column(name = "guest_type", nullable = false, length = 20)
    private String guestType; // INDIVIDUAL, FAMILY, GROUP

    @Column(name = "reservation_channel", nullable = false, length = 20)
    private String reservationChannel; // WEB, PHONE, OTA

    @Column(name = "request_note", length = 255)
    private String requestNote;

    // 금액
    @Column(name = "reservation_room_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal reservationRoomPrice;

    @Column(name = "reservation_package_price", precision = 10, scale = 2)
    private BigDecimal reservationPackagePrice;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    // 시간
    @Column(name = "reserved_at", nullable = false)
    private LocalDateTime reservedAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 연관 코드 (FK는 느슨하게 숫자로)
    @Column(name = "tenant_code", nullable = false)
    private Long tenantCode;

    @Column(name = "room_code", nullable = false)
    private Long roomCode;

    @Column(name = "customer_code", nullable = false)
    private Long customerCode;

    @Column(name = "package_code")
    private Long packageCode;



    public static Reservation createReservation(
            LocalDate checkin,
            LocalDate checkout,
            int guestCount,
            String guestType,
            String reservationChannel,
            BigDecimal roomPrice,
            BigDecimal packagePrice,
            Long tenantCode,
            Long roomCode,
            Long customerCode,
            Long packageCode,
            String reservationStatus
    ) {
        LocalDateTime now = LocalDateTime.now();

        BigDecimal totalPrice =
                roomPrice.add(packagePrice != null ? packagePrice : BigDecimal.ZERO);

        return Reservation.builder()
                .reservationStatus(reservationStatus)
                .checkinDate(checkin)
                .checkoutDate(checkout)
                .guestCount(guestCount)
                .guestType(guestType)
                .reservationChannel(reservationChannel)
                .reservationRoomPrice(roomPrice)
                .reservationPackagePrice(packagePrice)
                .totalPrice(totalPrice)
                .reservedAt(now)
                .createdAt(now)
                .tenantCode(tenantCode)
                .roomCode(roomCode)
                .customerCode(customerCode)
                .packageCode(packageCode)
                .build();
    }
}
