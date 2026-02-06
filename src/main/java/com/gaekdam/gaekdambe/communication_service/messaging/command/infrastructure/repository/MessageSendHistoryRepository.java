package com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageSendHistory;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageSendStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 메시지 발송 이력 Repository
 * - 발송 대상 조회
 * - 상태 선점(SCHEDULED → PROCESSING) 담당
 */
public interface MessageSendHistoryRepository
        extends JpaRepository<MessageSendHistory, Long> {

    /**
     * 발송 예정(SCHEDULED) 이고 현재 시각 이전인 메시지 ID 조회
     */
    @Query("""
    select h.sendCode
    from MessageSendHistory h
    where h.status = :status
      and h.scheduledAt <= :now
    order by h.sendCode
""")
    List<Long> findIdsByStatusAndScheduledAtBefore(
            @Param("status") MessageSendStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    /**
     * 현재 상태가 특정 값일 때만 다음 상태로 변경
     * - Worker 선점용 (분산락 역할)
     */
    @Modifying
    @Transactional
    @Query("""
        update MessageSendHistory h
        set h.status = :next,
            h.processingAt = :processingAt
        where h.sendCode = :sendCode
          and h.status = :current
    """)
    int updateStatusIfCurrent(
            @Param("sendCode") Long sendCode,
            @Param("current") MessageSendStatus current,
            @Param("next") MessageSendStatus next,
            @Param("processingAt") LocalDateTime processingAt
    );
}