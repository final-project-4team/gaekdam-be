package com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.publisher;

import com.gaekdam.gaekdambe.communication_service.messaging.command.application.service.MessageScheduleCommandService;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.event.MessageJourneyEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageJourneyEventPublisher {

    private final MessageScheduleCommandService scheduleService;

    @EventListener
    public void handle(MessageJourneyEvent event) {
        scheduleService.schedule(event);
    }
}
