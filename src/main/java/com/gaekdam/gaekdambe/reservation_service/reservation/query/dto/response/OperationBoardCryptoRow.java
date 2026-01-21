package com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response;

import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.enums.OperationStatus;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class OperationBoardCryptoRow {

    private Long reservationCode;
    private Long stayCode;

    private Long customerCode;
    private byte[] customerNameEnc;
    private byte[] dekEnc;

    private String propertyName;
    private String roomType;
    private LocalDate plannedCheckinDate;
    private LocalDate plannedCheckoutDate;
    private String operationStatus;
}
