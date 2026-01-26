package com.gaekdam.gaekdambe.communication_service.messaging.query.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MessageStageQueryMapper {

    String findStageNameEng(@Param("stageCode") Long stageCode);
}
