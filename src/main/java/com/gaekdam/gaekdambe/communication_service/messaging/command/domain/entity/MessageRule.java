package com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageChannel;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.ReferenceEntityType;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.VisitorType;
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
@Table(
        name = "message_rule",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_message_rule_stage_visitor",
                        columnNames = {
                                "hotel_group_code",
                                "stage_code",
                                "visitor_type"
                        }
                )
        }
)
public class MessageRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_code")
    private Long ruleCode;

    @Column(name = "reference_entity_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ReferenceEntityType referenceEntityType; // RESERVATION, STAY

    @Column(name = "offset_minutes", nullable = false)
    private int offsetMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "visitor_type", length = 10)
    private VisitorType visitorType; // FIRST, REPEAT (nullable)

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 10)
    private MessageChannel channel; // SMS, EMAIL, KAKAO, PUSH

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled;

    @Column(name = "priority", nullable = false)
    private int priority;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // FK (느슨하게 숫자)
    @Column(name = "stage_code", nullable = false)
    private Long stageCode;

    @Column(name = "template_code", nullable = false)
    private Long templateCode;

    @Column(name = "hotel_group_code", nullable = false)
    private Long hotelGroupCode;


    public void update(
            Long templateCode,
            int offsetMinutes,
            VisitorType visitorType,
            MessageChannel channel,
            boolean isEnabled,
            int priority,
            String description
    ) {
        this.templateCode = templateCode;
        this.offsetMinutes = offsetMinutes;
        this.visitorType = visitorType;
        this.channel = channel;
        this.isEnabled = isEnabled;
        this.priority = priority;
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public void disable() {
        this.isEnabled = false;
        this.updatedAt = LocalDateTime.now();
    }
}
