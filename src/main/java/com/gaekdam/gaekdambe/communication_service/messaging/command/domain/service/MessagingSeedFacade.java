package com.gaekdam.gaekdambe.communication_service.messaging.command.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessagingSeedFacade {

    private final MessageTemplateSeeder templateSeeder;
    private final MessageRuleSeeder ruleSeeder;

    @Transactional
    public void seedForHotelGroup(Long hotelGroupCode) {
        templateSeeder.seed(hotelGroupCode);
        ruleSeeder.seed(hotelGroupCode);
    }
}