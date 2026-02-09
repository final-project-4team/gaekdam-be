package com.gaekdam.gaekdambe.reservation_service.reservation.query.mapper;

import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.OperationBoardCryptoRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface TodayOperationMapper {

    /**
     * Today Operation Board 리스트 조회
     * 기준일은 애플리케이션(LocalDate) 기준
     */
    List<OperationBoardCryptoRow> findTodayOperations(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("propertyCode") Long propertyCode,
            @Param("summaryType") String summaryType,
            @Param("customerNameHash") String customerNameHash,
            @Param("reservationCode") String reservationCode,
            @Param("page") PageRequest page,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("sort") SortRequest sort
    );

    /**
     * Today Operation Status별 COUNT
     */
    List<Map<String, Object>> countTodayOperationsByStatus(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("propertyCode") Long propertyCode,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
