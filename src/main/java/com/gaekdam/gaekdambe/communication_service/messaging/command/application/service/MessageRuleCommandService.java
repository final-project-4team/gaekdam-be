package com.gaekdam.gaekdambe.communication_service.messaging.command.application.service;

import com.gaekdam.gaekdambe.communication_service.messaging.command.application.dto.request.MessageRuleCreateRequest;
import com.gaekdam.gaekdambe.communication_service.messaging.command.application.dto.request.MessageRuleUpdateRequest;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageRule;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MessageRuleCommandService {

    private final MessageRuleRepository repository;

    @Transactional
    public Long createRule(MessageRuleCreateRequest req) {

        LocalDateTime now = LocalDateTime.now();

        MessageRule rule = MessageRule.builder()
                .hotelGroupCode(req.getHotelGroupCode())
                .referenceEntityType(req.getReferenceEntityType())
                .offsetMinutes(req.getOffsetMinutes())
                .visitorType(req.getVisitorType())
                .channel(req.getChannel())
                .isEnabled(req.isEnabled())
                .priority(req.getPriority())
                .description(req.getDescription())
                .stageCode(req.getStageCode())
                .templateCode(req.getTemplateCode())
                .createdAt(now)
                .updatedAt(now)
                .build();

        repository.save(rule);
        return rule.getRuleCode();
    }

    @Transactional
    public void update(Long ruleCode, MessageRuleUpdateRequest req) {
        MessageRule rule = repository.findById(ruleCode)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleCode));

        rule.update(
                req.getTemplateCode(),
                req.getOffsetMinutes(),
                req.getVisitorType(),
                req.getChannel(),
                req.getIsEnabled(),
                req.getPriority(),
                req.getDescription()
        );
    }


    @Transactional
    public void disableRule(Long ruleCode) {

        MessageRule rule = repository.findById(ruleCode)
                .orElseThrow(() -> new IllegalArgumentException("룰 없음"));

        rule.disable();
    }
}
