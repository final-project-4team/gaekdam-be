package com.gaekdam.gaekdambe.reservation_service.stay.command.application.dto.request;

import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.enums.CheckInOutChannel;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.enums.SettlementYn;
import lombok.Getter;

@Getter
public class CheckInRequest {
    private Long reservationCode;
    private int guestCount;
    private String carNumber;
    private SettlementYn settlementYn; // Y / N
    private CheckInOutChannel recordChannel; // ADMIN, KIOSK 등
}
