package com.gaekdam.gaekdambe.unit.communication_service.messaging.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.gaekdam.gaekdambe.communication_service.messaging.query.service.MessageJourneyStageQueryService;
import com.gaekdam.gaekdambe.communication_service.messaging.query.mapper.MessageJourneyStageQueryMapper;
import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessageJourneyStageResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

class MessageJourneyStageQueryServiceTest {

    @Mock
    MessageJourneyStageQueryMapper mapper;

    private MessageJourneyStageQueryService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new MessageJourneyStageQueryService(mapper);
    }

    @Test
    void findAll_returnsList() {
        MessageJourneyStageResponse r1 = new MessageJourneyStageResponse();
        MessageJourneyStageResponse r2 = new MessageJourneyStageResponse();
        List<MessageJourneyStageResponse> rows = Arrays.asList(r1, r2);

        when(mapper.findAllStages()).thenReturn(rows);

        assertThat(service.findAll()).hasSize(2);
    }
}
