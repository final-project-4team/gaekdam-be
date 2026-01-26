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

    private Long stageCode;
    private Long reservationCode;
    private Long stayCode;
    private Long ruleCode;
    private Long templateCode;

    @Enumerated(EnumType.STRING)
    private MessageChannel channel;

    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    private MessageSendStatus status;

    private String failReason;
    private String externalMessageId;

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


    public void markSkipped(String reason) {
        this.status = MessageSendStatus.SKIPPED;
        this.failReason = reason;
    }

    public void markCancelled(String reason) {
        this.status = MessageSendStatus.CANCELLED;
        this.failReason = reason;
    }
}
