package com.gaekdam.gaekdambe.communication_service.messaging.policy;

import java.time.LocalTime;

/**
 * 체크인/체크아웃 기준 시각 정책
 * - 예약 테이블에는 DATE만 있으므로
 * - 실제 시간은 정책으로 조립한다
 */
public final class CheckinPolicyTime {

    // 호텔 기본 체크인 시간
    public static final LocalTime CHECKIN_TIME = LocalTime.of(15, 0);

    // 호텔 기본 체크아웃 시간
    public static final LocalTime CHECKOUT_TIME = LocalTime.of(11, 0);

    private CheckinPolicyTime() {}
}
