package com.gaekdam.gaekdambe.reservation_service.reservation.query.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

@Mapper
public interface ReservationSummaryMapper {
    long countAllToday(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("propertyCode") Long propertyCode,
            @Param("today") LocalDate today
    );

    long countTodayCheckIn(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("propertyCode") Long propertyCode,
            @Param("today") LocalDate today
    );

    long countTodayCheckOut(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("propertyCode") Long propertyCode,
            @Param("today") LocalDate today
    );

    long countStayingRooms(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("propertyCode") Long propertyCode,
            @Param("today") LocalDate today
    );
}