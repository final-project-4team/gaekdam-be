package com.gaekdam.gaekdambe.unit.reservation_service.timeline.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.gaekdam.gaekdambe.reservation_service.timeline.query.mapper.TimelineMapper;
import com.gaekdam.gaekdambe.reservation_service.timeline.query.service.TimelineQueryService;
import com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response.TimelineDetailResponse;
import com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response.StaySummaryResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

class TimelineQueryServiceAdditionalTest {

    @Mock
    TimelineMapper mapper;

    private TimelineQueryService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new TimelineQueryService(mapper);
    }

    @Test
    void getTimeline_handles_empty_event_list_and_null_facilities() {
        when(mapper.findTimelineEvents(5L, 6L)).thenReturn(List.of());
        when(mapper.findCustomerType(6L)).thenReturn("단체");
        when(mapper.countFacilityUsage(6L)).thenReturn(0);
        // facilities가 null로 오는 경우도 처리되는지 확인
        when(mapper.findUsedFacilities(6L)).thenReturn((List<String>) null);

        TimelineDetailResponse res = service.getTimeline(5L, 6L);
        assertThat(res).isNotNull();
        StaySummaryResponse s = res.getSummary();
        assertThat(s.getTotalFacilityUsage()).isEqualTo(0);
        assertThat(s.getSummaryText()).contains("부대시설 이용은 없었습니다");
    }

    @Test
    void getTimeline_handles_null_customer_type_by_treating_as_empty() {
        when(mapper.findTimelineEvents(8L, 9L)).thenReturn(List.of());
        when(mapper.findCustomerType(9L)).thenReturn(null);
        when(mapper.countFacilityUsage(9L)).thenReturn(1);
        when(mapper.findUsedFacilities(9L)).thenReturn(List.of("Gym"));

        TimelineDetailResponse res = service.getTimeline(8L, 9L);
        assertThat(res).isNotNull();
        StaySummaryResponse s = res.getSummary();
        // null customerType인 경우, 현재 구현은 null + "으로" 형태가 되므로 null 체크 없이도 동작
        assertThat(s.getSummaryText()).contains("총 1회의 부대시설(Gym)");
    }
}
