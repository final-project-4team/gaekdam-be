package com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.detail;

import lombok.Getter;

@Getter
public class CustomerContactCryptoRow {
    private byte[] contactValueEnc;
    private String contactValueHash;
}