package com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.enums;

public enum OperationStatus {
    RESERVED,          // 예약만 있음
    CHECKIN_PLANNED,   // stay 있음, actual_checkin_at 없음
    STAYING,           // actual_checkin_at 있음, actual_checkout_at 없음
    CHECKOUT_PLANNED,  // 오늘 checkout 예정
    COMPLETED          // actual_checkout_at 있음
}
