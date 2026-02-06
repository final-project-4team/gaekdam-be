package com.gaekdam.gaekdambe.communication_service.messaging.worker;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageSendHistory;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageSendStatus;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageSendHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * PROCESSING 상태의 메시지를 실제로 발송하고 결과를 반영한다.
 */
@Service
@RequiredArgsConstructor
public class MessageSendProcessor {

    private final MessageSendHistoryRepository repository;
    private final Map<String, MessageSender> senderMap;

    @Transactional
    public void processOne(Long sendCode) {

        MessageSendHistory history =
                repository.findById(sendCode)
                        .orElseThrow(() -> new IllegalStateException("History not found. sendCode=" + sendCode));

        // Worker에서 선점한 건만 처리
        if (history.getStatus() != MessageSendStatus.PROCESSING) {
            return;
        }

        MessageSender sender =
                senderMap.get(history.getChannel().name());

        if (sender == null) {
            // 채널 미지원 → FAILED
            history.markFailed("No sender for channel: " + history.getChannel());
            return;
        }

        try {
            // 실제 발송
            String externalMessageId = sender.send(history);

            // 성공
            history.markSent(externalMessageId);

        } catch (Exception e) {
            // 실패
            history.markFailed(e.getMessage());
        }
    }
}