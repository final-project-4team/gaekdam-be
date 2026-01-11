package com.gaekdam.gaekdambe.reservation_service.stay.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CheckInOutResponse {

    private Long checkinoutCode;
    private String recordType;
    private LocalDateTime recordedAt;

    private String recordChannel;
    private int guestCount;
    private String carNumber;
    private String settlementYn;

    private Long stayCode;
}
