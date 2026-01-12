package com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.detail;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerInfo {

    private Long customerCode;

//    // 암호화 필드
//    private String customerNameEnc;
//    private String customerNameHash;

    // 상태 정보
    private String nationalityType;
    private String contractType;
    private String customerStatus;

    // 화면용 (Service에서 채움)
    private String customerName;
}
