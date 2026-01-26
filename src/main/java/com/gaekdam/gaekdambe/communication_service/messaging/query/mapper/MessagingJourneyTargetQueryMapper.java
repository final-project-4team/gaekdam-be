package com.gaekdam.gaekdambe.communication_service.messaging.query.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessagingJourneyTargetQueryMapper {

    // 예약 확정
    List<Long> findReservationConfirmedTargets(@Param("stageCode") Long stageCode);

    // 예약 취소
    List<Long> findReservationCancelledTargets(@Param("stageCode") Long stageCode);

    // 노쇼
    List<Long> findNoShowTargets(@Param("stageCode") Long stageCode);

    // 체크인 등록(DB 기준)
    List<Long> findCheckInConfirmedStayTargets(@Param("stageCode") Long stageCode);

    // 체크아웃 등록(DB 기준)
    List<Long> findCheckOutConfirmedStayTargets(@Param("stageCode") Long stageCode);
}
