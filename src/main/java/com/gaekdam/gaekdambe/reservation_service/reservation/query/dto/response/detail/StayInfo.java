package com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.detail;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StayInfo {
    private String stayStatus;
    private int guestCount;
    private LocalDateTime actualCheckinAt;
    private LocalDateTime actualCheckoutAt;
}
