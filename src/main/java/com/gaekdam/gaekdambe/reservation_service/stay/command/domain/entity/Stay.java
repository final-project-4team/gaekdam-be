package com.gaekdam.gaekdambe.reservation_service.stay.command.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "stay")
public class Stay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stay_code")
    private Long stayCode;

    @Column(name = "stay_status", nullable = false, length = 20)
    private String stayStatus; // STAYING, COMPLETED

    @Column(name = "actual_checkin_at")
    private LocalDateTime actualCheckinAt;

    @Column(name = "actual_checkout_at")
    private LocalDateTime actualCheckoutAt;

    @Column(name = "guest_count", nullable = false)
    private int guestCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // FK
    @Column(name = "reservation_code", nullable = false)
    private Long reservationCode;

    @Column(name = "room_code", nullable = false)
    private Long roomCode;

    @Column(name = "customer_code", nullable = false)
    private Long customerCode;

    /* 생성 메서드 */
    public static Stay createStay(
            Long reservationCode,
            Long roomCode,
            Long customerCode,
            int guestCount,
            LocalDateTime checkinAt,
            LocalDateTime checkoutAt,
            String stayStatus
    ) {
        LocalDateTime now = LocalDateTime.now();

        return Stay.builder()
                .reservationCode(reservationCode)
                .roomCode(roomCode)
                .customerCode(customerCode)
                .guestCount(guestCount)
                .stayStatus(stayStatus)
                .actualCheckinAt(checkinAt)
                .actualCheckoutAt(checkoutAt)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
