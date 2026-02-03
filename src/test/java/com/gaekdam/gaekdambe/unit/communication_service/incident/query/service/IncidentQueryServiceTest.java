package com.gaekdam.gaekdambe.unit.communication_service.incident.query.service;

import com.gaekdam.gaekdambe.communication_service.incident.query.dto.request.IncidentListSearchRequest;
import com.gaekdam.gaekdambe.communication_service.incident.query.dto.response.*;
import com.gaekdam.gaekdambe.communication_service.incident.query.mapper.IncidentMapper;
import com.gaekdam.gaekdambe.communication_service.incident.query.service.IncidentQueryService;
import com.gaekdam.gaekdambe.global.crypto.DecryptionService;
import com.gaekdam.gaekdambe.global.crypto.SearchHashService;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class IncidentQueryServiceTest {

    private IncidentMapper incidentMapper;
    private DecryptionService decryptionService;
    private SearchHashService searchHashService;
    private IncidentQueryService service;

    @BeforeEach
    void setUp() {
        incidentMapper = mock(IncidentMapper.class);
        decryptionService = mock(DecryptionService.class);
        searchHashService = mock(SearchHashService.class);
        service = new IncidentQueryService(incidentMapper, decryptionService, searchHashService);
    }

    @Test
    @DisplayName("getIncidents: rows -> content 변환 후 PageResponse 반환, decrypt 호출")
    void getIncidents_success_decryptCalled() {
        // given
        PageRequest page = new PageRequest();
        page.setPage(1);
        page.setSize(20);

        IncidentListSearchRequest search = new IncidentListSearchRequest();
        SortRequest sort = new SortRequest();
        sort.setSortBy("created_at");
        sort.setDirection("DESC");

        IncidentListEncResponse row = mock(IncidentListEncResponse.class);
        when(row.employeeCode()).thenReturn(10L);
        when(row.employeeDekEnc()).thenReturn(new byte[]{1});
        when(row.employeeNameEnc()).thenReturn(new byte[]{2});
        when(decryptionService.decrypt(eq(10L), any(), any())).thenReturn("홍길동");

        when(incidentMapper.findIncidents(page, search, sort)).thenReturn(List.of(row));
        when(incidentMapper.countIncidents(search)).thenReturn(1L);

        // when
        PageResponse<IncidentListResponse> res = service.getIncidents(page, search, sort);

        // then
        assertThat(res.getContent()).hasSize(1);
        assertThat(res.getTotalElements()).isEqualTo(1L);
        assertThat(res.getPage()).isEqualTo(1);
        assertThat(res.getSize()).isEqualTo(20);

        verify(incidentMapper).findIncidents(page, search, sort);
        verify(incidentMapper).countIncidents(search);
        verify(decryptionService).decrypt(eq(10L), any(), any());
        verifyNoMoreInteractions(incidentMapper, decryptionService);
        verifyNoInteractions(searchHashService);
    }

    @Test
    @DisplayName("getIncidentDetail: row null이면 INVALID_REQUEST")
    void getIncidentDetail_null_thenThrow() {
        // given
        Long hotelGroupCode = 1L;
        Long incidentCode = 10L;
        when(incidentMapper.findIncidentDetail(hotelGroupCode, incidentCode)).thenReturn(null);

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.getIncidentDetail(hotelGroupCode, incidentCode),
                CustomException.class
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
        assertThat(ex.getMessage()).contains("존재하지 않는 사건/사고입니다.");

        verify(incidentMapper).findIncidentDetail(hotelGroupCode, incidentCode);
        verifyNoMoreInteractions(incidentMapper);
        verifyNoInteractions(decryptionService, searchHashService);
    }

    @Test
    @DisplayName("getIncidentDetail: 정상 조회면 decrypt 호출 후 반환")
    void getIncidentDetail_success_decryptCalled() {
        // given
        Long hotelGroupCode = 1L;
        Long incidentCode = 10L;

        IncidentDetailEncResponse row = mock(IncidentDetailEncResponse.class);
        when(row.employeeCode()).thenReturn(10L);
        when(row.employeeDekEnc()).thenReturn(new byte[]{1});
        when(row.employeeNameEnc()).thenReturn(new byte[]{2});
        when(decryptionService.decrypt(eq(10L), any(), any())).thenReturn("홍길동");

        when(incidentMapper.findIncidentDetail(hotelGroupCode, incidentCode)).thenReturn(row);

        // when
        IncidentDetailResponse res = service.getIncidentDetail(hotelGroupCode, incidentCode);

        // then
        assertThat(res).isNotNull();

        verify(incidentMapper).findIncidentDetail(hotelGroupCode, incidentCode);
        verify(decryptionService).decrypt(eq(10L), any(), any());
        verifyNoMoreInteractions(incidentMapper, decryptionService);
        verifyNoInteractions(searchHashService);
    }

    @Test
    @DisplayName("getIncidentActionHistories: rows -> response 변환, decrypt 호출")
    void getIncidentActionHistories_success_decryptCalled() {
        // given
        Long hotelGroupCode = 1L;
        Long incidentCode = 10L;

        IncidentActionHistoryEncResponse row = mock(IncidentActionHistoryEncResponse.class);
        when(row.employeeCode()).thenReturn(10L);
        when(row.employeeDekEnc()).thenReturn(new byte[]{1});
        when(row.employeeNameEnc()).thenReturn(new byte[]{2});
        when(row.actionContent()).thenReturn("조치");
        when(row.createdAt()).thenReturn(LocalDateTime.now());

        when(decryptionService.decrypt(eq(10L), any(), any())).thenReturn("홍길동");

        when(incidentMapper.findIncidentActionHistories(hotelGroupCode, incidentCode))
                .thenReturn(List.of(row));

        // when
        List<IncidentActionHistoryResponse> list =
                service.getIncidentActionHistories(hotelGroupCode, incidentCode);

        // then
        assertThat(list).hasSize(1);

        verify(incidentMapper).findIncidentActionHistories(hotelGroupCode, incidentCode);
        verify(decryptionService).decrypt(eq(10L), any(), any());
        verifyNoMoreInteractions(incidentMapper, decryptionService);
        verifyNoInteractions(searchHashService);
    }
}
