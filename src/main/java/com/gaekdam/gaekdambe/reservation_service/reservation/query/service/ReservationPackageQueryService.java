package com.gaekdam.gaekdambe.reservation_service.reservation.query.service;

import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.request.ReservationPackageSearchRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.ReservationPackageResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.mapper.ReservationPackageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationPackageQueryService {

    private final ReservationPackageMapper mapper;

    public PageResponse<ReservationPackageResponse> getPackages(
            PageRequest page,
            ReservationPackageSearchRequest search,
            SortRequest sort
    ) {
        List<ReservationPackageResponse> list =
                mapper.findPackages(page, search, sort);

        long total =
                mapper.countPackages(search);

        return new PageResponse<>(
                list,
                page.getPage(),
                page.getSize(),
                total
        );
    }
}
