package com.gaekdam.gaekdambe.unit.customer_service.membership.query.service;

import com.gaekdam.gaekdambe.customer_service.membership.query.dto.response.MembershipHistoryResponse;
import com.gaekdam.gaekdambe.customer_service.membership.query.mapper.MembershipHistoryMapper;
import com.gaekdam.gaekdambe.customer_service.membership.query.service.MembershipHistoryQueryService;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembershipHistoryQueryServiceTest {

    private MembershipHistoryMapper mapper;
    private MembershipHistoryQueryService service;

    @BeforeEach
    void setUp() {
        mapper = mock(MembershipHistoryMapper.class);
        service = new MembershipHistoryQueryService(mapper);
    }

    @Test
    @DisplayName("getHistory: from/to 둘 다 있고 from > to 이면 MEMBERSHIP_HISTORY_INVALID_PERIOD")
    void getHistory_invalidPeriod_thenThrow() {
        // given
        PageRequest page = new PageRequest();
        Long hotelGroupCode = 1L;
        Long customerCode = 100L;

        LocalDateTime from = LocalDateTime.of(2026, 1, 10, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 1, 1, 0, 0);

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.getHistory(page, hotelGroupCode, customerCode, from, to),
                CustomException.class
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBERSHIP_HISTORY_INVALID_PERIOD);
        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("getHistory: from/to 둘 다 있고 from == to 여도 정상 처리된다")
    void getHistory_equalPeriod_success() {
        // given
        PageRequest page = new PageRequest();
        page.setPage(1);
        page.setSize(20);

        Long hotelGroupCode = 1L;
        Long customerCode = 100L;

        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 1, 1, 0, 0);

        when(mapper.findHistory(page, hotelGroupCode, customerCode, from, to)).thenReturn(List.of());
        when(mapper.countHistory(hotelGroupCode, customerCode, from, to)).thenReturn(0L);

        // when
        PageResponse<MembershipHistoryResponse> result =
                service.getHistory(page, hotelGroupCode, customerCode, from, to);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        verify(mapper).findHistory(page, hotelGroupCode, customerCode, from, to);
        verify(mapper).countHistory(hotelGroupCode, customerCode, from, to);
        verifyNoMoreInteractions(mapper);
    }

    @Test
    @DisplayName("getHistory: 정상 케이스면 findHistory/countHistory 호출하고 PageResponse를 만든다")
    void getHistory_success_buildsPageResponse() {
        // given
        PageRequest page = new PageRequest();
        page.setPage(2);
        page.setSize(20);

        Long hotelGroupCode = 1L;
        Long customerCode = 100L;

        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 1, 31, 23, 59);

        List<MembershipHistoryResponse> list = List.of(
                new MembershipHistoryResponse(
                        LocalDateTime.of(2026, 1, 5, 10, 0),
                        "등급 변경",
                        "SILVER / ACTIVE => GOLD / ACTIVE",
                        "MANUAL",
                        10L
                )
        );
        long total = 41L;

        when(mapper.findHistory(page, hotelGroupCode, customerCode, from, to)).thenReturn(list);
        when(mapper.countHistory(hotelGroupCode, customerCode, from, to)).thenReturn(total);

        // when
        PageResponse<MembershipHistoryResponse> result =
                service.getHistory(page, hotelGroupCode, customerCode, from, to);

        // then
        assertThat(result.getContent()).isSameAs(list);
        assertThat(result.getPage()).isEqualTo(2);
        assertThat(result.getSize()).isEqualTo(20);
        assertThat(result.getTotalElements()).isEqualTo(41);
        assertThat(result.getTotalPages()).isEqualTo(3);

        verify(mapper).findHistory(page, hotelGroupCode, customerCode, from, to);
        verify(mapper).countHistory(hotelGroupCode, customerCode, from, to);
        verifyNoMoreInteractions(mapper);
    }

    @Test
    @DisplayName("getHistory: from=null, to!=null이면 기간검증 건너뛰고 mapper로 그대로 전달한다")
    void getHistory_fromNull_passThrough() {
        // given
        PageRequest page = new PageRequest();
        Long hotelGroupCode = 1L;
        Long customerCode = 100L;

        LocalDateTime from = null;
        LocalDateTime to = LocalDateTime.of(2026, 1, 31, 23, 59);

        when(mapper.findHistory(page, hotelGroupCode, customerCode, from, to)).thenReturn(List.of());
        when(mapper.countHistory(hotelGroupCode, customerCode, from, to)).thenReturn(0L);

        // when
        PageResponse<MembershipHistoryResponse> result =
                service.getHistory(page, hotelGroupCode, customerCode, from, to);

        // then
        assertThat(result.getTotalElements()).isZero();
        verify(mapper).findHistory(page, hotelGroupCode, customerCode, from, to);
        verify(mapper).countHistory(hotelGroupCode, customerCode, from, to);
        verifyNoMoreInteractions(mapper);
    }

    @Test
    @DisplayName("getHistory: from!=null, to=null이면 기간검증 건너뛰고 mapper로 그대로 전달한다")
    void getHistory_toNull_passThrough() {
        // given
        PageRequest page = new PageRequest();
        Long hotelGroupCode = 1L;
        Long customerCode = 100L;

        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime to = null;

        when(mapper.findHistory(page, hotelGroupCode, customerCode, from, to)).thenReturn(List.of());
        when(mapper.countHistory(hotelGroupCode, customerCode, from, to)).thenReturn(0L);

        // when
        PageResponse<MembershipHistoryResponse> result =
                service.getHistory(page, hotelGroupCode, customerCode, from, to);

        // then
        assertThat(result.getTotalElements()).isZero();
        verify(mapper).findHistory(page, hotelGroupCode, customerCode, from, to);
        verify(mapper).countHistory(hotelGroupCode, customerCode, from, to);
        verifyNoMoreInteractions(mapper);
    }
}
