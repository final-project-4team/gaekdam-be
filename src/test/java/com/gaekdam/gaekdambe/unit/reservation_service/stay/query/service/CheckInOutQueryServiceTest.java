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
import com.gaekdam.gaekdambe.reservation_service.stay.query.dto.request.CheckInOutSearchRequest;
import com.gaekdam.gaekdambe.reservation_service.stay.query.dto.response.CheckInOutResponse;
import com.gaekdam.gaekdambe.reservation_service.stay.query.mapper.CheckInOutMapper;
import com.gaekdam.gaekdambe.reservation_service.stay.query.service.CheckInOutQueryService;

class CheckInOutQueryServiceTest {

    @Mock
    CheckInOutMapper mapper;

    private CheckInOutQueryService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new CheckInOutQueryService(mapper);
    }

    @Test
    void getCheckInOuts_delegatesToMapper_and_returnsPageResponse() {
        PageRequest pr = new PageRequest(); pr.setPage(1); pr.setSize(10);
        CheckInOutSearchRequest search = new CheckInOutSearchRequest();
        SortRequest sort = new SortRequest();

        CheckInOutResponse r1 = new CheckInOutResponse();
        CheckInOutResponse r2 = new CheckInOutResponse();
        when(mapper.findCheckInOuts(pr, search, sort)).thenReturn(List.of(r1, r2));
        when(mapper.countCheckInOuts(search)).thenReturn(2L);

        PageResponse<CheckInOutResponse> res = service.getCheckInOuts(pr, search, sort);
        assertThat(res).isNotNull();
        // PageResponse uses 'content' and 'totalElements' fields
        assertThat(res.getContent()).hasSize(2);
        assertThat(res.getTotalElements()).isEqualTo(2L);
    }
}
