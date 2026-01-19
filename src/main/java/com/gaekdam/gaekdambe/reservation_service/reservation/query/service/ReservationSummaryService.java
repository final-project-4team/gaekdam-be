package com.gaekdam.gaekdambe.reservation_service.reservation.query.service;

import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.TodayReservationSummaryResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.mapper.ReservationSummaryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ReservationSummaryService {

    private final ReservationSummaryMapper summaryMapper;

    public TodayReservationSummaryResponse getTodaySummary(
            Long hotelGroupCode,
            Long propertyCode
    ) {
        LocalDate today = LocalDate.now();

        return new TodayReservationSummaryResponse(
                summaryMapper.countAllToday(hotelGroupCode, propertyCode, today),
                summaryMapper.countTodayCheckIn(hotelGroupCode, propertyCode, today),
                summaryMapper.countTodayCheckOut(hotelGroupCode, propertyCode, today),
                summaryMapper.countStayingRooms(hotelGroupCode, propertyCode, today)
        );
    }
}
