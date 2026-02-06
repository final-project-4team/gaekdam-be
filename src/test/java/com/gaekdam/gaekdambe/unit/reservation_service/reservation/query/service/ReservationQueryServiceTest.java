package com.gaekdam.gaekdambe.unit.reservation_service.reservation.query.service;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.request.ReservationSearchRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.ReservationResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.mapper.ReservationMapper;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.service.ReservationQueryService;

class ReservationQueryServiceTest {

    @Mock
    ReservationMapper mapper;

    private ReservationQueryService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ReservationQueryService(mapper);
    }

    @Test
    void getReservations_delegatesToMapper_and_returnsPageResponse() {
        PageRequest pr = new PageRequest(); pr.setPage(1); pr.setSize(10);
        ReservationSearchRequest search = new ReservationSearchRequest();
        SortRequest sort = new SortRequest();

        ReservationResponse r1 = new ReservationResponse();
        ReservationResponse r2 = new ReservationResponse();
        when(mapper.findReservations(pr, search, sort)).thenReturn(List.of(r1, r2));
        when(mapper.countReservations(search)).thenReturn(2L);

        PageResponse<ReservationResponse> res = service.getReservations(pr, search, sort);
        assertThat(res).isNotNull();
        assertThat(res.getContent()).hasSize(2);
        assertThat(res.getTotalElements()).isEqualTo(2L);
    }
}
