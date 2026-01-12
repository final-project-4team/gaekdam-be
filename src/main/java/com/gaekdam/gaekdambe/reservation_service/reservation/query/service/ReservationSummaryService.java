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

    public TodayReservationSummaryResponse getTodaySummary(Long hotelGroupCode) {
        LocalDate today = LocalDate.now();

        return new TodayReservationSummaryResponse(
                summaryMapper.countAllToday(hotelGroupCode, today),
                summaryMapper.countTodayCheckIn(hotelGroupCode, today),
                summaryMapper.countTodayCheckOut(hotelGroupCode, today),
                summaryMapper.countStayingRooms(hotelGroupCode)
        );
    }
}
