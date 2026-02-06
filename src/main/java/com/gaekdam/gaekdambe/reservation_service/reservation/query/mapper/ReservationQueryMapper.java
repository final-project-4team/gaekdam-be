package com.gaekdam.gaekdambe.reservation_service.reservation.query.mapper;

import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.DemoReservationResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReservationQueryMapper {

    /**
     * 투숙(stay) 없는 예약 1건 조회 (문자 시연용)
     */
    DemoReservationResponse findOneReservationWithoutStay(
            @Param("hotelGroupCode") Long hotelGroupCode
    );
}

