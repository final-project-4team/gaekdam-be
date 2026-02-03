package com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TimelineDetailResponse {

    private List<TimelineEventResponse> events;
    private StaySummaryResponse summary;

}