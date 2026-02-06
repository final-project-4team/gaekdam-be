package com.gaekdam.gaekdambe.operation_service.facility.query.service;

import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;

import com.gaekdam.gaekdambe.operation_service.facility.query.dto.request.FacilitySearchRequest;
import com.gaekdam.gaekdambe.operation_service.facility.query.dto.response.FacilityResponse;
import com.gaekdam.gaekdambe.operation_service.facility.query.mapper.FacilityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FacilityQueryService {

    private final FacilityMapper mapper;

    public PageResponse<FacilityResponse> getFacilities(
            PageRequest page,
            FacilitySearchRequest search,
            SortRequest sort
    ) {
        List<FacilityResponse> list =
                mapper.findFacilities(page, search, sort);

        long total =
                mapper.countFacilities(search);

        return new PageResponse<>(
                list,
                page.getPage(),
                page.getSize(),
                total
        );
    }
}
