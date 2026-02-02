package com.gaekdam.gaekdambe.unit.reservation_service.timeline.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.gaekdam.gaekdambe.reservation_service.timeline.query.mapper.TimelineMapper;
import com.gaekdam.gaekdambe.reservation_service.timeline.query.service.TimelineQueryService;
import com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response.TimelineEventResponse;
import com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response.TimelineDetailResponse;
import com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response.StaySummaryResponse;
import com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response.CustomerStayResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

class TimelineQueryServiceTest {

    @Mock
    TimelineMapper mapper;

    private TimelineQueryService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new TimelineQueryService(mapper);
    }

    @Test
    void getTimeline_buildsSummary_with_no_facility_usage() {
        when(mapper.findTimelineEvents(1L, 2L)).thenReturn(List.of(new TimelineEventResponse()));
        when(mapper.findCustomerType(2L)).thenReturn("VIP");
        when(mapper.countFacilityUsage(2L)).thenReturn(0);
        when(mapper.findUsedFacilities(2L)).thenReturn(List.of());

        var res = service.getTimeline(1L, 2L);

        assertThat(res).isNotNull();
        StaySummaryResponse s = res.getSummary();
        assertThat(s.getCustomerType()).isEqualTo("VIP");
        assertThat(s.getTotalFacilityUsage()).isEqualTo(0);
        assertThat(s.getSummaryText()).contains("부대시설 이용은 없었습니다");
    }

    @Test
    void getTimeline_buildsSummary_with_facilities() {
        when(mapper.findTimelineEvents(1L, 3L)).thenReturn(List.of(new TimelineEventResponse()));
        when(mapper.findCustomerType(3L)).thenReturn("일반");
        when(mapper.countFacilityUsage(3L)).thenReturn(2);
        when(mapper.findUsedFacilities(3L)).thenReturn(List.of("Pool","Spa"));

        var res = service.getTimeline(1L, 3L);

        assertThat(res).isNotNull();
        StaySummaryResponse s = res.getSummary();
        assertThat(s.getTotalFacilityUsage()).isEqualTo(2);
        assertThat(s.getSummaryText()).contains("총 2회의 부대시설(Pool, Spa)을 이용했습니다");
    }

    @Test
    void getCustomerStays_delegatesToMapper() {
        CustomerStayResponse r = new CustomerStayResponse(1L, "STAYING", java.time.LocalDateTime.now(), null, 101, 2);
        when(mapper.findCustomerStays(10L, 20L)).thenReturn(List.of(r));

        var res = service.getCustomerStays(10L, 20L);
        assertThat(res).hasSize(1);
        assertThat(res.get(0)).isSameAs(r);
    }
}
