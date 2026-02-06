package com.gaekdam.gaekdambe.reservation_service.stay.query.service;


import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.reservation_service.stay.query.dto.request.StaySearchRequest;
import com.gaekdam.gaekdambe.reservation_service.stay.query.dto.response.StayResponse;
import com.gaekdam.gaekdambe.reservation_service.stay.query.mapper.StayMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StayQueryService {

    private final StayMapper stayMapper;

    public PageResponse<StayResponse> getStays(
            PageRequest page,
            StaySearchRequest search,
            SortRequest sort
    ) {
        List<StayResponse> list =
                stayMapper.findStays(page, search, sort);

        long total =
                stayMapper.countStays(search);

        return new PageResponse<>(
                list,
                page.getPage(),
                page.getSize(),
                total
        );
    }
}