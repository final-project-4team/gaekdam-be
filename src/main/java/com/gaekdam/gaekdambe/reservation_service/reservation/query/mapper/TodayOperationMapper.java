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

    List<OperationBoardCryptoRow> findTodayOperations(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("propertyCode") Long propertyCode,
            @Param("summaryType") String summaryType,
            @Param("page") PageRequest page,
            @Param("today") LocalDate today,
            @Param("sort") SortRequest sort
    );

    List<Map<String, Object>> countTodayOperationsByStatus(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("propertyCode") Long propertyCode,
            @Param("today") LocalDate today
    );

}
