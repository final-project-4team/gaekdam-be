package com.gaekdam.gaekdambe.communication_service.messaging.query.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MessageSendValidationQueryMapper {

    boolean isReservationStillValid(
            @Param("reservationCode") Long reservationCode
    );

    boolean isCheckinPlannedStillValid(
            @Param("reservationCode") Long reservationCode
    );

    boolean isCheckoutPlannedStillValid(
            @Param("stayCode") Long stayCode
    );
}
