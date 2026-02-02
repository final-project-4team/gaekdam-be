package com.gaekdam.gaekdambe.unit.reservation_service.stay.query.service;

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
import com.gaekdam.gaekdambe.reservation_service.stay.query.dto.request.StaySearchRequest;
import com.gaekdam.gaekdambe.reservation_service.stay.query.dto.response.StayResponse;
import com.gaekdam.gaekdambe.reservation_service.stay.query.mapper.StayMapper;
import com.gaekdam.gaekdambe.reservation_service.stay.query.service.StayQueryService;

class StayQueryServiceTest {

    @Mock
    StayMapper mapper;

    private StayQueryService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new StayQueryService(mapper);
    }

    @Test
    void getStays_delegatesToMapper_and_returnsPageResponse() {
        PageRequest pr = new PageRequest(); pr.setPage(1); pr.setSize(10);
        StaySearchRequest search = new StaySearchRequest();
        SortRequest sort = new SortRequest();

        StayResponse r1 = new StayResponse();
        StayResponse r2 = new StayResponse();
        when(mapper.findStays(pr, search, sort)).thenReturn(List.of(r1, r2));
        when(mapper.countStays(search)).thenReturn(2L);

        PageResponse<StayResponse> res = service.getStays(pr, search, sort);
        assertThat(res).isNotNull();
        assertThat(res.getContent()).hasSize(2);
        assertThat(res.getTotalElements()).isEqualTo(2L);
    }
}
