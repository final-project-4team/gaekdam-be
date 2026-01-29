package com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRuleRepository
        extends JpaRepository<MessageRule, Long> {
    List<MessageRule> findByHotelGroupCode(Long hotelGroupCode);

    boolean existsByHotelGroupCodeAndStageCodeAndTemplateCode(
            Long hotelGroupCode,
            Long stageCode,
            Long templateCode
    );
}
