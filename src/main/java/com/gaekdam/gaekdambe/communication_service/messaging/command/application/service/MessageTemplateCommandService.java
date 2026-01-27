package com.gaekdam.gaekdambe.communication_service.messaging.command.application.service;

import com.gaekdam.gaekdambe.communication_service.messaging.command.application.dto.request.MessageTemplateCreateRequest;
import com.gaekdam.gaekdambe.communication_service.messaging.command.application.dto.request.MessageTemplateUpdateRequest;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageTemplate;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
public class MessageTemplateCommandService {

    private final MessageTemplateRepository repository;

    @Transactional
    public Long createTemplate(MessageTemplateCreateRequest req, Long propertyCode) {
        LocalDateTime now = LocalDateTime.now();

        MessageTemplate template = MessageTemplate.builder()
                .visitorType(req.getVisitorType())
                .languageCode(req.getLanguageCode())
                .title(req.getTitle())
                .content(req.getContent())
                .conditionExpr(req.getConditionExpr())
                .isActive(req.isActive())
                .membershipGradeCode(req.getMembershipGradeCode())
                .propertyCode(propertyCode)
                .stageCode(req.getStageCode())
                .createdAt(now)
                .updatedAt(now)
                .build();

        repository.save(template);
        return template.getTemplateCode();
    }

    @Transactional
    public void update(Long templateCode, MessageTemplateUpdateRequest req) {

        MessageTemplate template = repository.findById(templateCode)
                .orElseThrow(() -> new IllegalArgumentException("Template not found"));

        log.info("BEFORE isActive = {}", template.isActive());
        log.info("REQ isActive = {}", req.getIsActive());

        template.update(
                req.getTitle(),
                req.getContent(),
                req.getLanguageCode(),
                req.getIsActive(),
                req.getConditionExpr()
        );

        log.info("AFTER isActive = {}", template.isActive());
    }
}
