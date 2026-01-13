package com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity;


import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageSendStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "message_send_history")
public class MessageSendHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "send_id")
    private Long sendId;

    /** 고객여정단계 코드 */
    @Column(name = "stage_code", nullable = false)
    private Long stageCode;

    /** 예약 식별자 (선택) */
    @Column(name = "reservation_code")
    private Long reservationCode;

    /** 투숙 식별자 (선택) */
    @Column(name = "stay_code")
    private Long stayCode;

    /** 적용된 메시지 룰 */
    @Column(name = "rule_code", nullable = false)
    private Long ruleCode;

    /** 사용된 템플릿 */
    @Column(name = "template_code", nullable = false)
    private Long templateCode;

    /** 발송 예정 시각 */
    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    /** 실제 발송 시각 */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    /** 발송 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private MessageSendStatus status;

    /** 실패 사유 */
    @Column(name = "fail_reason", length = 500)
    private String failReason;

    /** 외부 메시지 시스템 ID (SMS, 카카오 등) */
    @Column(name = "external_message_id", length = 100)
    private String externalMessageId;
}
