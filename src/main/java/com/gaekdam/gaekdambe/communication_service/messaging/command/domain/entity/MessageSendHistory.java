package com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageChannel;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageSendStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 메시지 발송 이력
 * - 메시징 시스템의 단일 진실 소스
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "message_send_history",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_msg_reservation_stage_rule",
                        columnNames = {"stage_code", "rule_code", "reservation_code"}
                ),
                @UniqueConstraint(
                        name = "uk_msg_stay_stage_rule",
                        columnNames = {"stage_code", "rule_code", "stay_code"}
                )
        }
)
public class MessageSendHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "send_code")
    private Long sendCode;

    @Column(name = "stage_code", nullable = false)
    private Long stageCode;

    @Column(name = "reservation_code")
    private Long reservationCode;

    @Column(name = "stay_code")
    private Long stayCode;

    @Column(name = "rule_code", nullable = false)
    private Long ruleCode;

    @Column(name = "template_code", nullable = false)
    private Long templateCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 10)
    private MessageChannel channel;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    /**
     * Worker가 메시지를 선점한 시각
     */
    @Column(name = "processing_at")
    private LocalDateTime processingAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MessageSendStatus status;

    @Column(name = "fail_reason", length = 255)
    private String failReason;

    @Column(name = "external_message_id", length = 100)
    private String externalMessageId;


    @Column(name = "from_phone", length = 20)
    private String fromPhone;

    @Column(name = "to_phone", length = 20)
    private String toPhone;




    /* =====================
       상태 변경 메서드
       ===================== */

    /**
     * 발송 처리 시작 (PROCESSING)
     */
    public void markProcessing() {
        this.status = MessageSendStatus.PROCESSING;
        this.processingAt = LocalDateTime.now();
    }

    /**
     * 발송 성공 (SENT)
     */
    public void markSent(String externalId) {
        this.status = MessageSendStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.externalMessageId = externalId;
    }

    /**
     * 발송 실패 (FAILED)
     */
    public void markFailed(String reason) {
        this.status = MessageSendStatus.FAILED;
        this.failReason = reason;
    }

    /**
     * 조건 불충족 등으로 발송 스킵
     */
    public void markSkipped(String reason) {
        this.status = MessageSendStatus.SKIPPED;
        this.failReason = reason;
    }

    /**
     * 취소 처리
     */
    public void markCancelled(String reason) {
        this.status = MessageSendStatus.CANCELLED;
        this.failReason = reason;
    }
}