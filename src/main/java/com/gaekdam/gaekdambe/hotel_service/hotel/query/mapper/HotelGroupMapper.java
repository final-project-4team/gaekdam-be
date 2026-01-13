package com.gaekdam.gaekdambe.hotel_service.hotel.query.mapper;

import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.hotel_service.hotel.query.dto.request.HotelGroupSearchRequest;
import com.gaekdam.gaekdambe.hotel_service.hotel.query.dto.response.HotelGroupListResponse;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface HotelGroupMapper {

  List<HotelGroupListResponse> findHotelGroupList(
      @Param("page") PageRequest page,
      @Param("search") HotelGroupSearchRequest search,
      @Param("sort") SortRequest sort);

  long countHotelGroupList(
      @Param("search") HotelGroupSearchRequest search);
}
