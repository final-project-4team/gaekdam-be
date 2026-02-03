package com.gaekdam.gaekdambe.communication_service.messaging.query.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 메시지 여정 스케줄링용 예약/투숙 조회 Mapper
 */
@Mapper
public interface MessagingPlannedJourneyTargetQueryMapper {

    /**
     * 오늘 체크인 예정 예약 코드 조회
     */
    List<Long> findTodayCheckinPlannedReservationCodes(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("today") String today,
            @Param("stageCode") Long stageCode
    );

    /**
     * 오늘 체크아웃 예정 투숙 코드 조회
     */
    List<Long> findTodayCheckoutPlannedStayCodes(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("today") String today,
            @Param("stageCode") Long stageCode
    );
}
