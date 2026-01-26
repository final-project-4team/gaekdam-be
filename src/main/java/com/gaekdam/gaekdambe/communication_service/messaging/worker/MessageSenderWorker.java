package com.gaekdam.gaekdambe.communication_service.messaging.worker;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageSendHistory;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageSendStatus;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageSendHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MessageSenderWorker {

    private final MessageSendHistoryRepository repository;
    private final Map<String, MessageSender> senderMap;

    /**
     * SCHEDULED 상태 메시지 발송
     */
    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void work() {

        List<MessageSendHistory> targets =
                repository.findTop100ByStatusAndScheduledAtBefore(
                        MessageSendStatus.SCHEDULED,
                        LocalDateTime.now()
                );

        for (MessageSendHistory history : targets) {
            try {
                // 선점 (트랜잭션 내 엔티티 상태 변경)
                history.markProcessing();

                MessageSender sender =
                        senderMap.get(history.getChannel().name());

                if (sender == null) {
                    throw new IllegalStateException(
                            "No sender for channel: " + history.getChannel()
                    );
                }

                // 실제 발송
                String externalMessageId = sender.send(history);

                history.markSent(externalMessageId);

            } catch (Exception e) {
                history.markFailed(e.getMessage());
            }
        }
    }
}
