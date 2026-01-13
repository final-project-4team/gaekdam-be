package com.gaekdam.gaekdambe.hotel_service.hotel.query.service;

import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.hotel_service.hotel.query.dto.request.HotelGroupQueryRequest;
import com.gaekdam.gaekdambe.hotel_service.hotel.query.dto.request.HotelGroupSearchRequest;
import com.gaekdam.gaekdambe.hotel_service.hotel.query.dto.response.HotelGroupListResponse;
import com.gaekdam.gaekdambe.hotel_service.hotel.query.mapper.HotelGroupMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HotelGroupQueryService {

  private final HotelGroupMapper hotelGroupMapper;

  public PageResponse<HotelGroupListResponse> getHotelGroupList(HotelGroupQueryRequest query) {
    PageRequest pageReq = new PageRequest();
    pageReq.setPage(query.page());
    pageReq.setSize(query.size());

    HotelGroupSearchRequest searchReq = new HotelGroupSearchRequest(query.hotelGroupCode(), query.hotelGroupName());

    SortRequest sortReq = new SortRequest();
    sortReq.setSortBy(query.sortBy());
    sortReq.setDirection(query.direction());

    List<HotelGroupListResponse> list = hotelGroupMapper.findHotelGroupList(pageReq, searchReq, sortReq);
    long total = hotelGroupMapper.countHotelGroupList(searchReq);

    return new PageResponse<>(
        list,
        query.page(),
        query.size(),
        total);
  }

}
