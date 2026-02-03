package com.gaekdam.gaekdambe.reservation_service.stay.command.application.dto.request;

import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.enums.CheckInOutChannel;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.enums.SettlementYn;
import lombok.Getter;

@Getter
public class CheckOutRequest {
    private Long stayCode;
    private String carNumber;
    private SettlementYn settlementYn;
    private CheckInOutChannel recordChannel;
}
