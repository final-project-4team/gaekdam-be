package com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response;

import lombok.Getter;

@Getter
public class DemoReservationCryptoRow {
    private Long reservationCode;
    private String reservationStatus;

    private Long customerCode;   // DEK 캐시 키
    private byte[] dekEnc;        // customer.dek_enc
    private byte[] phoneEnc;      // customer_contact.contact_value_enc
}
