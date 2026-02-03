package com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.detail;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CheckInOutInfo {
    private LocalDateTime checkInAt;
    private LocalDateTime checkOutAt;
}