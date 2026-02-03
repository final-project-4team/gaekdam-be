package com.gaekdam.gaekdambe.reservation_service.timeline.query.mapper;

import com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response.CustomerStayResponse;
import com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response.StaySummaryResponse;
import com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response.TimelineEventResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TimelineMapper {

    // 고객 → 투숙 리스트
    List<CustomerStayResponse> findCustomerStays(Long hotelGroupCode,Long customerCode );

    // 투숙 → 타임라인 이벤트
    List<TimelineEventResponse> findTimelineEvents(Long hotelGroupCode ,Long stayCode);


    // 투숙 → 요약 카드
    String findCustomerType(Long stayCode);

    int countFacilityUsage(Long stayCode);

    List<String> findUsedFacilities(Long stayCode);
}
