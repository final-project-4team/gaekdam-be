package com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageSendHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageSendHistoryRepository
        extends JpaRepository<MessageSendHistory, Long> {
}
