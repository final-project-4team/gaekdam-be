package com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.detail;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerInfo {

    private Long customerCode;

    // 상태 정보
    private String nationalityType;
    private String contractType;
    private String customerStatus;

    // 화면용 (Service에서 채움)
    private String customerName;

    private Boolean isMember;        // 멤버 여부
    private String phoneNumber;
}
