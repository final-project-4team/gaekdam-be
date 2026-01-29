package com.gaekdam.gaekdambe.communication_service.messaging.query.mapper;

import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessageJourneyStageResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageJourneyStageQueryMapper {

    List<MessageJourneyStageResponse> findAllStages();
}
