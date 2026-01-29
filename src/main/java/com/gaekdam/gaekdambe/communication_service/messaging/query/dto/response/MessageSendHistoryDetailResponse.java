package com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageChannel;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageSendStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 메시지 발송 이력 상세 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class MessageSendHistoryDetailResponse {

    private Long sendCode;                 // 발송 이력 PK
    private Long stageCode;                // 여정 코드
    private String stageNameKor;           // 여정명(한글)

    private Long templateCode;             // 템플릿 코드
    private String templateTitle;          // 템플릿 제목
    private String templateContent;        // 템플릿 본문

    private Long reservationCode;          // 예약 코드
    private Long stayCode;                 // 투숙 코드

    private Long propertyCode;             // 지점 코드
    private String propertyName;           // 지점명

    private MessageChannel channel;        // 채널
    private MessageSendStatus status;      // 상태

    private LocalDateTime scheduledAt;     // 예약 시각
    private LocalDateTime processingAt;    // 처리 시작 시각
    private LocalDateTime sentAt;          // 발송 완료 시각

    private String failReason;             // 실패/스킵 사유
    private String externalMessageId;      // 외부 메시지 ID
}
