package com.gaekdam.gaekdambe.unit.reservation_service.reservation.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

import java.util.Collections;

import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.mapper.ReservationMapper;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.request.ReservationSearchRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.ReservationResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.service.ReservationQueryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ReservationQueryServiceAdditionalTest {

    @Mock
    ReservationMapper mapper;

    private ReservationQueryService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ReservationQueryService(mapper);
    }

    @Test
    void getReservations_returnsEmptyPage_when_noResults() {
        // 빈 결과일 때 PageResponse가 올바르게 생성되는지 검증
        PageRequest pr = new PageRequest(); pr.setPage(1); pr.setSize(10);
        ReservationSearchRequest search = new ReservationSearchRequest();
        SortRequest sort = new SortRequest();

        when(mapper.findReservations(pr, search, sort)).thenReturn(Collections.emptyList());
        when(mapper.countReservations(search)).thenReturn(0L);

        PageResponse<ReservationResponse> res = service.getReservations(pr, search, sort);
        assertThat(res).isNotNull();
        assertThat(res.getContent()).isEmpty();
        assertThat(res.getTotalElements()).isEqualTo(0L);
        assertThat(res.getTotalPages()).isEqualTo(0);
    }

    @Test
    void getReservations_handlesNullSearch_and_delegates() {
        // search 파라미터가 null인 경우에도 mapper에 그대로 전달되어 동작하는지 확인
        PageRequest pr = new PageRequest(); pr.setPage(1); pr.setSize(5);
        SortRequest sort = new SortRequest();

        ReservationResponse r = new ReservationResponse();
        when(mapper.findReservations(pr, null, sort)).thenReturn(Collections.singletonList(r));
        when(mapper.countReservations(null)).thenReturn(1L);

        // 예외를 던지지 않고 정상 동작해야 함
        assertDoesNotThrow(() -> {
            PageResponse<ReservationResponse> res = service.getReservations(pr, null, sort);
            assertThat(res.getContent()).hasSize(1);
            assertThat(res.getTotalElements()).isEqualTo(1L);
        });
    }
}
