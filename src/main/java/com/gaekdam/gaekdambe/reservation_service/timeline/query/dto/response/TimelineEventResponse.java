package com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TimelineEventResponse {

    private String eventType;      // TimelineEventType name()
    private LocalDateTime occurredAt;

    // 공통 정보
    private Integer count;
    private String roomNumber;
    private String channel;

    // 선택 정보
    private String facilityName;
    private String facilityType;
}