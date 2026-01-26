package com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums;

public enum MessageSendStatus {
    SCHEDULED,    // 발송 대기
    PROCESSING,   // 워커가 선점하여 처리 중
    SENT,         // 발송 성공
    FAILED,       // 발송 실패
    SKIPPED,      // 조건 불충족으로 스킵
    CANCELLED     // 취소됨
}
