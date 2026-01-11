package com.gaekdam.gaekdambe.reservation_service.stay.query.mapper;


import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.reservation_service.stay.query.dto.request.StaySearchRequest;
import com.gaekdam.gaekdambe.reservation_service.stay.query.dto.response.StayResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StayMapper {

    List<StayResponse> findStays(
            @Param("page") PageRequest page,
            @Param("search") StaySearchRequest search,
            @Param("sort") SortRequest sort
    );

    long countStays(
            @Param("search") StaySearchRequest search
    );
}
