package com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TimelineEventResponse {

    private String eventType;     // RESERVATION, CHECK_IN, CHECK_OUT, FACILITY_USAGE
    private String title;         // 카드 제목
    private String description;   // 사람이 읽는 설명 문장
    private LocalDateTime occurredAt;

    // 선택 필드 (이벤트별 의미 다름)
    private Integer count;        // 인원수 등
    private String extra;         // 객실 정보, 시설 타입, 채널 등
}