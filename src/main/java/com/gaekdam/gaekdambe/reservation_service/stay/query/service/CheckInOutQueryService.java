package com.gaekdam.gaekdambe.reservation_service.stay.query.service;

import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.reservation_service.stay.query.dto.request.CheckInOutSearchRequest;
import com.gaekdam.gaekdambe.reservation_service.stay.query.dto.response.CheckInOutResponse;
import com.gaekdam.gaekdambe.reservation_service.stay.query.mapper.CheckInOutMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckInOutQueryService {

    private final CheckInOutMapper checkInOutMapper;

    public PageResponse<CheckInOutResponse> getCheckInOuts(
            PageRequest page,
            CheckInOutSearchRequest search,
            SortRequest sort
    ) {
        List<CheckInOutResponse> list =
                checkInOutMapper.findCheckInOuts(page, search, sort);

        long total =
                checkInOutMapper.countCheckInOuts(search);

        return new PageResponse<>(
                list,
                page.getPage(),
                page.getSize(),
                total
        );
    }
}
