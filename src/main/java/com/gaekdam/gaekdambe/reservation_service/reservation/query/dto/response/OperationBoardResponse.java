package com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response;

import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.enums.OperationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class OperationBoardResponse {

    private Long reservationCode;
    private Long stayCode;

//    // DB 조회용 (내부)
//    private byte[] customerNameEnc;
//    private String kmsKeyId;
//    private byte[] dekEnc;

    // API 응답용
    private String customerName;
    private Long customerCode;

    private String propertyName;
    private String roomType;
    private LocalDate plannedCheckinDate;
    private LocalDate plannedCheckoutDate;
    private String operationStatus;
}
