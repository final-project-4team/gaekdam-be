package com.gaekdam.gaekdambe.reservation_service.timeline.query.service;


import com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response.CustomerStayResponse;
import com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response.StaySummaryResponse;
import com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response.TimelineDetailResponse;
import com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response.TimelineEventResponse;
import com.gaekdam.gaekdambe.reservation_service.timeline.query.mapper.TimelineMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimelineQueryService {

    private final TimelineMapper mapper;

    public List<CustomerStayResponse> getCustomerStays(
            Long hotelGroupCode,
            Long customerCode
    ) {
        return mapper.findCustomerStays(hotelGroupCode, customerCode);
    }
    public TimelineDetailResponse getTimeline(
            Long hotelGroupCode,
            Long stayCode
    ) {
        List<TimelineEventResponse> events =
                mapper.findTimelineEvents(hotelGroupCode, stayCode);

        String customerType = mapper.findCustomerType(stayCode);
        int totalUsage = mapper.countFacilityUsage(stayCode);
        List<String> facilities = mapper.findUsedFacilities(stayCode);

        String summaryText = buildSummaryText(
                customerType,
                totalUsage,
                facilities
        );

        StaySummaryResponse summary = new StaySummaryResponse(
                customerType,
                totalUsage,
                facilities,
                summaryText
        );

        return TimelineDetailResponse.builder()
                .events(events)
                .summary(summary)
                .build();
    }

    private String buildSummaryText(
            String customerType,
            int totalFacilityUsage,
            List<String> facilities
    ) {
        String safeCustomerType = customerType == null ? "" : customerType;
        List<String> safeFacilities = facilities == null ? new ArrayList<>() : facilities;

        if (totalFacilityUsage == 0) {
            return safeCustomerType + "으로, 투숙 기간 동안 부대시설 이용은 없었습니다.";
        }

        return safeCustomerType +
                "으로, 투숙 기간 동안 총 " +
                totalFacilityUsage +
                "회의 부대시설(" +
                String.join(", ", safeFacilities) +
                ")을 이용했습니다.";
    }
}
