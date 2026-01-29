package com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.LanguageCode;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.VisitorType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "message_template")
public class MessageTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_code")
    private Long templateCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "visitor_type", nullable = false, length = 10)
    private VisitorType visitorType;

    // languageCode는 현재는 표시용이며
    // 다국어 템플릿 분기는 향후 확장 예정
    @Enumerated(EnumType.STRING)
    @Column(name = "language_code", length = 10)
    private LanguageCode languageCode;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "condition_expr", length = 500)
    private String conditionExpr;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * FK 영역
     */

    @Column(name = "hotel_group_code", nullable = false)
    private Long hotelGroupCode;

    @Column(name = "stage_code", nullable = false)
    private Long stageCode;


    public void update(
            String title,
            String content,
            LanguageCode languageCode,
            Boolean isActive,
            String conditionExpr
    ) {
        this.title = title;
        this.content = content;
        this.languageCode = languageCode;
        this.conditionExpr = conditionExpr;

        if (isActive != null) {
            this.isActive = isActive;
        }

        this.updatedAt = LocalDateTime.now();
    }


}
