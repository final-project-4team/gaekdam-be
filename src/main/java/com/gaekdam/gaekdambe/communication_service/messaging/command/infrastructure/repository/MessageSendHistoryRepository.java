package com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageSendHistory;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageSendStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageSendHistoryRepository
        extends JpaRepository<MessageSendHistory, Long> {

    List<MessageSendHistory>
    findTop100ByStatusAndScheduledAtBefore(
            MessageSendStatus status,
            LocalDateTime now
    );
}
