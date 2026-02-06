package com.gaekdam.gaekdambe.reservation_service.reservation.query.service;


import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.DemoReservationResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.mapper.ReservationQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationDemoQueryService {

    private final ReservationQueryMapper reservationQueryMapper;

    public DemoReservationResponse getDemoReservation(Long hotelGroupCode) {
        return reservationQueryMapper.findOneReservationWithoutStay(hotelGroupCode);
    }

}
