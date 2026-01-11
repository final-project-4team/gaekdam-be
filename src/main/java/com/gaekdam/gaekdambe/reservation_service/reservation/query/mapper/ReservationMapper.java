package com.gaekdam.gaekdambe.reservation_service.reservation.query.mapper;

import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.request.ReservationSearchRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.ReservationResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReservationMapper {

    List<ReservationResponse> findReservations(
            @Param("page") PageRequest page,
            @Param("search") ReservationSearchRequest search,
            @Param("sort") SortRequest sort
    );

    long countReservations(
            @Param("search") ReservationSearchRequest search
    );

}
