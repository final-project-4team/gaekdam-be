package com.gaekdam.gaekdambe.communication_service.messaging.query.mapper;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.VisitorType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MessagingVisitorTypeQueryMapper {

    /**
     * 고객의 방문 타입 판정
     * FIRST / REPEAT
     */
    VisitorType resolveVisitorType(@Param("reservationCode") Long reservationCode);
}
