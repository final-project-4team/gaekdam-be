package com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class StaySummaryResponse {

    private String customerType;      // 신규 고객 / 재방문 고객
    private int totalFacilityUsage;   // 부대시설 총 이용 횟수
    private List<String> facilities;  // 이용한 부대시설 목록
    private String summaryText; // 요약 내용
}
