package com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageJourneyStage;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageJourneyStageRepository
        extends JpaRepository<MessageJourneyStage, Long> {

    Optional<MessageJourneyStage> findByStageNameEng(String stageNameEng);


    boolean existsByStageNameEng(String stageNameEng);


    List<MessageJourneyStage> findByIsActiveTrue();
}