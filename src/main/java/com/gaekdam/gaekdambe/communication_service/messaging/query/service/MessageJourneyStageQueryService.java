package com.gaekdam.gaekdambe.communication_service.messaging.query.service;

import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessageJourneyStageResponse;
import com.gaekdam.gaekdambe.communication_service.messaging.query.mapper.MessageJourneyStageQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageJourneyStageQueryService {

    private final MessageJourneyStageQueryMapper mapper;

    public List<MessageJourneyStageResponse> findAll() {
        return mapper.findAllStages();
    }
}
