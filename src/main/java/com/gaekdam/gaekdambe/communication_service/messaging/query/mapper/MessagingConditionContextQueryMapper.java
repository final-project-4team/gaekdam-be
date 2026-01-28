package com.gaekdam.gaekdambe.communication_service.messaging.query.mapper;

import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessagingConditionContext;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MessagingConditionContextQueryMapper {

    MessagingConditionContext findByReservationCode(@Param("reservationCode") Long reservationCode);

    MessagingConditionContext findByStayCode(@Param("stayCode") Long stayCode);
}
