package com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageChannel;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageSendStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "message_send_history",
        uniqueConstraints = {
                // 예약 기반 메시지 중복 방지
                @UniqueConstraint(
                        name = "uk_msg_reservation_stage_rule",
                        columnNames = {
                                "stage_code",
                                "rule_code",
                                "reservation_code"
                        }
                ),
                // 투숙 기반 메시지 중복 방지
                @UniqueConstraint(
                        name = "uk_msg_stay_stage_rule",
                        columnNames = {
                                "stage_code",
                                "rule_code",
                                "stay_code"
                        }
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

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private MessageSendStatus status;

    @Column(name = "fail_reason", length = 500)
    private String failReason;

    @Column(name = "external_message_id", length = 100)
    private String externalMessageId;

    /* =========================
       상태 변경 메서드
       ========================= */

    public void markProcessing() {
        this.status = MessageSendStatus.PROCESSING;
    }

    public void markSent(String externalId) {
        this.status = MessageSendStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.externalMessageId = externalId;
    }

    public void markFailed(String reason) {
        this.status = MessageSendStatus.FAILED;
        this.failReason = reason;
    }
}
