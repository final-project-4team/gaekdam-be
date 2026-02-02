package com.gaekdam.gaekdambe.unit.reservation_service.reservation.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.mapper.ReservationPackageMapper;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.request.ReservationPackageSearchRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.ReservationPackageResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.service.ReservationPackageQueryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

class ReservationPackageQueryServiceTest {

    @Mock
    ReservationPackageMapper mapper;

    private ReservationPackageQueryService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ReservationPackageQueryService(mapper);
    }

    @Test
    void getPackages_delegatesToMapper_and_returnsPageResponse() {
        PageRequest pr = new PageRequest(); pr.setPage(1); pr.setSize(10);
        ReservationPackageSearchRequest search = new ReservationPackageSearchRequest();
        SortRequest sort = new SortRequest();

        ReservationPackageResponse p1 = new ReservationPackageResponse();
        ReservationPackageResponse p2 = new ReservationPackageResponse();
        when(mapper.findPackages(pr, search, sort)).thenReturn(List.of(p1, p2));
        when(mapper.countPackages(search)).thenReturn(2L);

        PageResponse<ReservationPackageResponse> res = service.getPackages(pr, search, sort);
        assertThat(res).isNotNull();
        assertThat(res.getContent()).hasSize(2);
        assertThat(res.getTotalElements()).isEqualTo(2L);
    }
}
