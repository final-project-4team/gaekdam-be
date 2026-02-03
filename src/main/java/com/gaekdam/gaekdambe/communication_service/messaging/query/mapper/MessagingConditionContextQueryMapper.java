package com.gaekdam.gaekdambe.communication_service.messaging.query.mapper;

import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessagingConditionContext;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

@Mapper
public interface MessagingConditionContextQueryMapper {

    MessagingConditionContext findByReservationCode(
            @Param("reservationCode") Long reservationCode
    );

    MessagingConditionContext findByStayCode(
            @Param("stayCode") Long stayCode
    );

    /**
     * 체크인 예정 메시지용 날짜 조회
     */
    LocalDate findCheckinDateByReservationCode(
            @Param("reservationCode") Long reservationCode
    );

    /**
     * 체크아웃 예정 메시지용 날짜 조회
     */
    LocalDate findCheckoutDateByStayCode(
            @Param("stayCode") Long stayCode
    );
}
