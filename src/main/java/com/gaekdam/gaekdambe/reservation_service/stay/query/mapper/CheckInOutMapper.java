package com.gaekdam.gaekdambe.reservation_service.stay.query.mapper;


import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.reservation_service.stay.query.dto.request.CheckInOutSearchRequest;
import com.gaekdam.gaekdambe.reservation_service.stay.query.dto.response.CheckInOutResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CheckInOutMapper {

    List<CheckInOutResponse> findCheckInOuts(
            @Param("page") PageRequest page,
            @Param("search") CheckInOutSearchRequest search,
            @Param("sort") SortRequest sort
    );

    long countCheckInOuts(
            @Param("search") CheckInOutSearchRequest search
    );
}
