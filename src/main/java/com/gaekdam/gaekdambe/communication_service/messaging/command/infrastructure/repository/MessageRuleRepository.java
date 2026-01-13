package com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRuleRepository
        extends JpaRepository<MessageRule, Long> {
}
