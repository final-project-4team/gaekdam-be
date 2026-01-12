package com.gaekdam.gaekdambe.operation_service.facility.query.service;


import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.operation_service.facility.query.dto.request.FacilityUsageSearchRequest;
import com.gaekdam.gaekdambe.operation_service.facility.query.dto.response.FacilityUsageResponse;
import com.gaekdam.gaekdambe.operation_service.facility.query.mapper.FacilityUsageMapper;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FacilityUsageQueryService {

    private final FacilityUsageMapper facilityUsageMapper;

    public PageResponse<FacilityUsageResponse> getFacilityUsages(
            PageRequest page,
            FacilityUsageSearchRequest search,
            SortRequest sort
    ) {
        List<FacilityUsageResponse> list =
                facilityUsageMapper.findFacilityUsages(page, search, sort);

        long total =
                facilityUsageMapper.countFacilityUsages(search);

        return new PageResponse<>(
                list,
                page.getPage(),
                page.getSize(),
                total
        );
    }
}
