package com.gaekdam.gaekdambe.reservation_service.reservation.query.mapper;

import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.request.ReservationPackageSearchRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.ReservationPackageResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReservationPackageMapper {

    List<ReservationPackageResponse> findPackages(
            @Param("page") PageRequest page,
            @Param("search") ReservationPackageSearchRequest search,
            @Param("sort") SortRequest sort
    );

    long countPackages(
            @Param("search") ReservationPackageSearchRequest search
    );
}
