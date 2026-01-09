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
@Table(name = "checkinout")
public class CheckInOut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checkinout_code")
    private Long checkinoutCode;

    @Column(name = "record_type", nullable = false, length = 20)
    private String recordType; // CHECK_IN, CHECK_OUT

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "record_channel", length = 20)
    private String recordChannel; // FRONT, KIOSK, MOBILE

    @Column(name = "guest_count", nullable = false)
    private int guestCount;

    @Column(name = "car_number", length = 20)
    private String carNumber;

    @Column(name = "settlement_yn", nullable = false, length = 2)
    private String settlementYn; // Y, N

    @Column(name = "stay_code", nullable = false)
    private Long stayCode;


    /* 생성 메서드 */
    public static CheckInOut createCheckInOut(
            String recordType,
            LocalDateTime recordedAt,
            int guestCount,
            String recordChannel,
            String settlementYn,
            Long stayCode
    ) {
        return CheckInOut.builder()
                .recordType(recordType)
                .recordedAt(recordedAt)
                .guestCount(guestCount)
                .recordChannel(recordChannel)
                .settlementYn(settlementYn)
                .stayCode(stayCode)
                .build();
    }
}
