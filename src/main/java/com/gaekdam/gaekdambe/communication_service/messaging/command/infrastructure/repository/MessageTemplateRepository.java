package com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageTemplate;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.VisitorType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageTemplateRepository
        extends JpaRepository<MessageTemplate, Long> {
    List<MessageTemplate> findByStageCode(Long stageCode);

    boolean existsByHotelGroupCodeAndStageCodeAndVisitorType(
            Long hotelGroupCode,
            Long stageCode,
            VisitorType visitorType
    );


    List<MessageTemplate> findByHotelGroupCodeAndStageCode(
            Long hotelGroupCode,
            Long stageCode
    );
}