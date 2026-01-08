package com.gaekdam.gaekdambe.reservation_service.reservation.query.mapper;

import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.ReservationPackageResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.ReservationResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReservationPackageMapper {
    List<ReservationPackageResponse> findAllReservationPackages();
}
