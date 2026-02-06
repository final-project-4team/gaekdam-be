package com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CustomerStayResponse {

    private Long stayCode;
    private String stayStatus;

    private LocalDateTime actualCheckinAt;
    private LocalDateTime actualCheckoutAt;

    private Integer roomNumber;

    private int guestCount;
}
