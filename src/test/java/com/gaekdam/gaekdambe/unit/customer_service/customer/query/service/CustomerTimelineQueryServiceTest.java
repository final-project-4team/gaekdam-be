package com.gaekdam.gaekdambe.unit.customer_service.customer.query.service;

import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.CustomerTimelineResponse;
import com.gaekdam.gaekdambe.customer_service.customer.query.mapper.CustomerTimelineMapper;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.CustomerTimelineQueryService;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.row.CustomerTimelineRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerTimelineQueryServiceTest {

    private CustomerTimelineMapper timelineMapper;
    private CustomerTimelineQueryService service;

    @BeforeEach
    void setUp() {
        timelineMapper = mock(CustomerTimelineMapper.class);
        service = new CustomerTimelineQueryService(timelineMapper);
    }

    @Test
    @DisplayName("getTimeline: limit이 0이면 safeLimit=1로 보정해서 mapper 호출")
    void getTimeline_limitLowerBound() {
        // given
        CustomerTimelineRow row = mock(CustomerTimelineRow.class);
        when(row.getEventType()).thenReturn("EVENT");
        when(row.getOccurredAt()).thenReturn(LocalDateTime.now());
        when(row.getRefId()).thenReturn(1L);
        when(row.getTitle()).thenReturn("title");
        when(row.getSummary()).thenReturn("summary");

        when(timelineMapper.findCustomerTimeline(1L, 100L, 1)).thenReturn(List.of(row));

        // when
        CustomerTimelineResponse res = service.getTimeline(1L, 100L, 0);

        // then
        assertThat(res).isNotNull();
        verify(timelineMapper).findCustomerTimeline(1L, 100L, 1);

        Object limit = read(res, "getLimit", "limit");
        assertThat(limit).isEqualTo(1);

        Object items = read(res, "getItems", "items");
        assertThat(items).isInstanceOf(List.class);
        assertThat((List<?>) items).hasSize(1);
    }

    @Test
    @DisplayName("getTimeline: limit이 500이면 safeLimit=200 상한 적용해서 mapper 호출")
    void getTimeline_limitUpperBound() {
        // given
        when(timelineMapper.findCustomerTimeline(anyLong(), anyLong(), anyInt()))
                .thenReturn(List.of());

        ArgumentCaptor<Integer> limitCap = ArgumentCaptor.forClass(Integer.class);

        // when
        service.getTimeline(1L, 100L, 500);

        // then
        verify(timelineMapper).findCustomerTimeline(eq(1L), eq(100L), limitCap.capture());
        assertThat(limitCap.getValue()).isEqualTo(200);
    }

    private static Object read(Object target, String... candidates) {
        for (String name : candidates) {
            try {
                Method m = target.getClass().getMethod(name);
                return m.invoke(target);
            } catch (Exception ignore) {
            }
        }
        throw new IllegalStateException("Cannot read property methods from " + target.getClass());
    }
}
