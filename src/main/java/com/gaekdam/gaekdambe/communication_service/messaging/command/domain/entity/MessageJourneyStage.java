package com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "message_journey_stage")
public class MessageJourneyStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stage_code")
    private Long stageCode;

    @Column(name = "stage_name_eng", nullable = false, length = 40)
    private String stageNameEng;

    @Column(name = "stage_name_kor", nullable = false, length = 200)
    private String stageNameKor;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;
}
