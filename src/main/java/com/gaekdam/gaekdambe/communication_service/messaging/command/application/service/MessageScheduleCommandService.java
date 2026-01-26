package com.gaekdam.gaekdambe.communication_service.messaging.command.application.service;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageRule;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.event.MessageJourneyEvent;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageRuleRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class MessageScheduleCommandService {

    private final MessageRuleRepository ruleRepository;
    private final HistorySaveService historySaveService;

    public void schedule(MessageJourneyEvent event) {

        List<MessageRule> rules =
                ruleRepository.findAll().stream()
                        .filter(MessageRule::isEnabled)
                        .filter(r -> r.getStageCode().equals(event.getStageCode()))
                        .toList();

        for (MessageRule rule : rules) {
            historySaveService.saveHistory(event, rule);
        }
    }
}
