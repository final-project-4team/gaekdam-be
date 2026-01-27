package com.gaekdam.gaekdambe.communication_service.messaging.worker;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageSendStatus;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageSendHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MessageSenderWorker {

    private final MessageSendHistoryRepository repository;
    private final MessageSendProcessor processor;

    @Scheduled(fixedDelay = 10_000)
    public void work() {

        System.out.println("MessageSenderWorker tick");

        List<Long> targetIds =
                repository.findTop100IdsByStatusAndScheduledAtBefore(
                        MessageSendStatus.SCHEDULED,
                        LocalDateTime.now()
                );

        System.out.println("targets size = " + targetIds.size());

        for (Long sendCode : targetIds) {
            try {
                processor.processOne(sendCode);
            } catch (Exception e) {
                System.out.println(
                        "Message send failed. sendCode=" + sendCode
                                + ", reason=" + e.getMessage()
                );
            }
        }
    }
}
