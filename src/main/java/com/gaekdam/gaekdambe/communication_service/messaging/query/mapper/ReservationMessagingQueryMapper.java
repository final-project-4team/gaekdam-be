package com.gaekdam.gaekdambe.communication_service.messaging.query.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReservationMessagingQueryMapper {

    /**
     * 오늘 체크인 예정 (아직 체크인 안 됨)
     */
    List<Long> findTodayCheckinPlannedReservationCodes(
            @Param("today") String today
    );

    /**
     * 오늘 체크아웃 예정 (체크인 완료, 체크아웃 전)
     */
    List<Long> findTodayCheckoutPlannedStayCodes(
            @Param("today") String today
    );
}
