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

    /** FK 영역 */
    @Column(name = "membership_grade_code", nullable = false)
    private Long membershipGradeCode;

    @Column(name = "property_code", nullable = false)
    private Long propertyCode;

    @Column(name = "stage_code", nullable = false)
    private Long stageCode;


    public void update(
            String title,
            String content,
            LanguageCode languageCode,
            boolean isActive,
            String conditionExpr
    ) {
        this.title = title;
        this.content = content;
        this.languageCode = languageCode;
        this.isActive = isActive;
        this.conditionExpr = conditionExpr;
        this.updatedAt = LocalDateTime.now();
    }

    public void disable() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

}
