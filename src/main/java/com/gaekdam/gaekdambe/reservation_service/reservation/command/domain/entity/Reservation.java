package com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity;

import jakarta.persistence.*;
import lombok.*;

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
    private String reservationStatus;

    @Column(name = "checkin_date", nullable = false)
    private LocalDate checkinDate;

    @Column(name = "checkout_date", nullable = false)
    private LocalDate checkoutDate;

    @Column(name = "guest_count", nullable = false)
    private Integer guestCount;

    @Column(name = "guest_type", nullable = false, length = 20)
    private String guestType;

    @Column(name = "reservation_channel", nullable = false, length = 20)
    private String reservationChannel;

    @Column(name = "request_note", length = 255)
    private String requestNote;

    @Column(name = "reserved_at", nullable = false)
    private LocalDateTime reservedAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "tenant_code", nullable = false)
    private Long tenantCode;

    @Column(name = "room_code", nullable = false)
    private Long roomCode;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "package_code")
    private Long packageCode;

    // 생성 메서드 (예약 생성)
    public static Reservation createReservation(
            Long tenantCode,
            Long roomCode,
            Long customerId,
            LocalDate checkinDate,
            LocalDate checkoutDate,
            Integer guestCount,
            String guestType,
            String reservationChannel,
            String requestNote,
            Long packageCode
    ) {
        LocalDateTime now = LocalDateTime.now();

        return Reservation.builder()
                .tenantCode(tenantCode)
                .roomCode(roomCode)
                .customerId(customerId)
                .checkinDate(checkinDate)
                .checkoutDate(checkoutDate)
                .guestCount(guestCount != null ? guestCount : 1)
                .guestType(guestType)
                .reservationChannel(reservationChannel)
                .requestNote(requestNote)
                .packageCode(packageCode)
                .reservationStatus("RESERVED")
                .reservedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
