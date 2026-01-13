package com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageTemplateRepository
        extends JpaRepository<MessageTemplate, Long> {
    List<MessageTemplate> findByStageCode(Long stageCode);
}