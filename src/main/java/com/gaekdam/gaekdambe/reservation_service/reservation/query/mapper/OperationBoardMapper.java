package com.gaekdam.gaekdambe.reservation_service.reservation.query.mapper;

import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.request.OperationBoardSearchRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.OperationBoardCryptoRow;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.OperationBoardResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OperationBoardMapper {

    List<OperationBoardCryptoRow> findOperationBoard(
            @Param("page") PageRequest page,
            @Param("search") OperationBoardSearchRequest search,
            @Param("sort") SortRequest sort
    );

    long countOperationBoard(
            @Param("search") OperationBoardSearchRequest search
    );
}