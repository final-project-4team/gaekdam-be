package com.gaekdam.gaekdambe.communication_service.messaging.worker;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageSendStatus;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageSendHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SCHEDULED 상태의 메시지를 선점(PROCESSING) 후 발송 처리한다.
 */
@Component
@RequiredArgsConstructor
public class MessageSenderWorker {

    private static final Logger log = LoggerFactory.getLogger(MessageSenderWorker.class);

    private final MessageSendHistoryRepository repository;
    private final MessageSendProcessor processor;

    @Scheduled(cron = "0 */1 * * * *")
    public void work() {

        log.info("MessageSenderWorker tick");

        List<Long> targetIds =
                repository.findIdsByStatusAndScheduledAtBefore(
                        MessageSendStatus.SCHEDULED,
                        LocalDateTime.now(),
                        PageRequest.of(0, 100)
                );

        log.info("targets size = {}", targetIds.size());

        for (Long sendCode : targetIds) {
            processSafely(sendCode);
        }
    }

    /**
     * 단건 메시지를 안전하게 처리한다.
     */
    private void processSafely(Long sendCode) {
        try {
            processOneWithLock(sendCode);
        } catch (Exception e) {
            log.error("Message send failed. sendCode={}", sendCode, e);
        }
    }

    /**
     * 상태 선점(PROCESSING) 후 실제 발송을 수행한다.
     */
    @Transactional
    protected void processOneWithLock(Long sendCode) {

        // 1. SCHEDULED → PROCESSING 선점
        int updated =
                repository.updateStatusIfCurrent(
                        sendCode,
                        MessageSendStatus.SCHEDULED,
                        MessageSendStatus.PROCESSING,
                        LocalDateTime.now()
                );

        // 이미 다른 워커가 집었거나 상태 변경됨
        if (updated == 0) {
            return;
        }

        // 2. 실제 발송 처리 (내부에서 SENT / FAILED 처리)
        processor.processOne(sendCode);
    }
}