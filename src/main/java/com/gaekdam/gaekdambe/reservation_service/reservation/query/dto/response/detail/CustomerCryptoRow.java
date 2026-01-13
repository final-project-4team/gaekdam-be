package com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.detail;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CustomerCryptoRow {

    private Long customerCode;

    // DB 전용 (암호화 데이터)
    private byte[] customerNameEnc;
    private byte[] dekEnc;
    private String customerNameHash;

    // 상태 정보
    private String nationalityType;
    private String contractType;
    private String customerStatus;
}
