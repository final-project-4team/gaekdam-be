package com.gaekdam.gaekdambe.reservation_service.stay.command.domain.entity;

import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.enums.CheckInOutChannel;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.enums.CheckInOutRecordType;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.enums.SettlementYn;
import jakarta.persistence.*;
import lombok.*;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false, length = 20)
    private CheckInOutRecordType recordType;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_channel", length = 20)
    private CheckInOutChannel recordChannel;

    @Column(name = "guest_count", nullable = false)
    private int guestCount;

    @Column(name = "car_number", length = 20)
    private String carNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_yn", nullable = false, length = 2)
    private SettlementYn settlementYn;

    @Column(name = "stay_code", nullable = false)
    private Long stayCode;

    /* 생성 메서드 */
    public static CheckInOut createCheckInOut(
            CheckInOutRecordType recordType,
            LocalDateTime recordedAt,
            int guestCount,
            CheckInOutChannel recordChannel,
            SettlementYn settlementYn,
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
