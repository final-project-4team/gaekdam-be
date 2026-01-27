package com.gaekdam.gaekdambe.communication_service.messaging.worker;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageSendHistory;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageSendStatus;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageSendHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageSendProcessor {

    private final MessageSendHistoryRepository repository;
    private final Map<String, MessageSender> senderMap;

    @Transactional
    public void processOne(Long sendCode) {

        MessageSendHistory history =
                repository.findById(sendCode)
                        .orElseThrow(() -> new IllegalStateException("History not found"));

        // 이미 다른 워커가 집었으면 스킵
        if (history.getStatus() != MessageSendStatus.SCHEDULED) {
            return;
        }

        // 선점
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

        // 완료
        history.markSent(externalMessageId);
    }
}
