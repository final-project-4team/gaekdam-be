package com.gaekdam.gaekdambe.unit.customer_service.loyalty.query.service;

import com.gaekdam.gaekdambe.customer_service.loyalty.query.dto.response.LoyaltyHistoryResponse;
import com.gaekdam.gaekdambe.customer_service.loyalty.query.mapper.LoyaltyHistoryMapper;
import com.gaekdam.gaekdambe.customer_service.loyalty.query.service.LoyaltyHistoryQueryService;
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
class LoyaltyHistoryQueryServiceTest {

    private LoyaltyHistoryMapper mapper;
    private LoyaltyHistoryQueryService service;

    @BeforeEach
    void setUp() {
        mapper = mock(LoyaltyHistoryMapper.class);
        service = new LoyaltyHistoryQueryService(mapper);
    }

    @Test
    @DisplayName("history: from > to 이면 LOYALTY_HISTORY_INVALID_PERIOD")
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
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.LOYALTY_HISTORY_INVALID_PERIOD);
        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("history: 정상 조회면 list/total로 PageResponse 생성")
    void getHistory_success() {
        // given
        PageRequest page = new PageRequest();
        page.setPage(1);
        page.setSize(20);

        Long hotelGroupCode = 1L;
        Long customerCode = 100L;
        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 1, 31, 23, 59);

        List<LoyaltyHistoryResponse> mockList = List.of(mock(LoyaltyHistoryResponse.class));
        when(mapper.findHistory(page, hotelGroupCode, customerCode, from, to)).thenReturn(mockList);
        when(mapper.countHistory(hotelGroupCode, customerCode, from, to)).thenReturn(123L);

        // when
        PageResponse<LoyaltyHistoryResponse> res =
                service.getHistory(page, hotelGroupCode, customerCode, from, to);

        // then
        assertThat(res.getPage()).isEqualTo(1);
        assertThat(res.getSize()).isEqualTo(20);
        assertThat(res.getTotalElements()).isEqualTo(123L);
        assertThat(res.getContent()).isEqualTo(mockList);

        verify(mapper).findHistory(page, hotelGroupCode, customerCode, from, to);
        verify(mapper).countHistory(hotelGroupCode, customerCode, from, to);
        verifyNoMoreInteractions(mapper);
    }
}
