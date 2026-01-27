package com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageSendHistory;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageSendStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageSendHistoryRepository
        extends JpaRepository<MessageSendHistory, Long> {

    @Query("""
        select h.sendCode
        from MessageSendHistory h
        where h.status = :status
          and h.scheduledAt <= :now
        order by h.sendCode
    """)
    List<Long> findTop100IdsByStatusAndScheduledAtBefore(
            MessageSendStatus status,
            LocalDateTime now
    );
}
