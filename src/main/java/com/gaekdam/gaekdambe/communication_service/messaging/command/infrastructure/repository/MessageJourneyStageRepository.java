package com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageJourneyStage;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageJourneyStageRepository
        extends JpaRepository<MessageJourneyStage, Long> {
}