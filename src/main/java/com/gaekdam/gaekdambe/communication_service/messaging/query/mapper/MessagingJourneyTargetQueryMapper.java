package com.gaekdam.gaekdambe.communication_service.messaging.query.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 여정 상태 변경 대상 조회 (hotel_group 기준)
 */
@Mapper
public interface MessagingJourneyTargetQueryMapper {

    List<Long> findReservationConfirmedTargets(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("stageCode") Long stageCode
    );

    List<Long> findReservationCancelledTargets(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("stageCode") Long stageCode
    );

    List<Long> findNoShowTargets(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("stageCode") Long stageCode
    );

    List<Long> findCheckInConfirmedStayTargets(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("stageCode") Long stageCode
    );

    List<Long> findCheckOutConfirmedStayTargets(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("stageCode") Long stageCode
    );
}
