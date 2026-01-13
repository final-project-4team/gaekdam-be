package com.gaekdam.gaekdambe.reservation_service.timeline.query.service;

import com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response.CustomerStayResponse;
import com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response.StaySummaryResponse;
import com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response.TimelineDetailResponse;
import com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response.TimelineEventResponse;

import com.gaekdam.gaekdambe.reservation_service.timeline.query.mapper.TimelineMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class TimelineQueryService {

    private final TimelineMapper mapper;

    public List<CustomerStayResponse> getCustomerStays(Long customerCode) {
        return mapper.findCustomerStays(customerCode);
    }

    public TimelineDetailResponse getTimeline(Long stayCode) {

        List<TimelineEventResponse> events =
                mapper.findTimelineEvents(stayCode);

        StaySummaryResponse summary =
                new StaySummaryResponse(
                        mapper.findCustomerType(stayCode),
                        mapper.countFacilityUsage(stayCode),
                        mapper.findUsedFacilities(stayCode)
                );

        return new TimelineDetailResponse(events, summary);
    }
}