package com.gaekdam.gaekdambe.unit.customer_service.loyalty.query.service;

import com.gaekdam.gaekdambe.customer_service.loyalty.command.infrastructure.repository.LoyaltyGradeRepository;
import com.gaekdam.gaekdambe.customer_service.loyalty.query.dto.response.LoyaltyGradeDetailQueryResponse;
import com.gaekdam.gaekdambe.customer_service.loyalty.query.dto.response.LoyaltyGradeListQueryResponse;
import com.gaekdam.gaekdambe.customer_service.loyalty.query.mapper.LoyaltyGradeMapper;
import com.gaekdam.gaekdambe.customer_service.loyalty.query.service.LoyaltyGradeQueryService;
import com.gaekdam.gaekdambe.customer_service.membership.query.mapper.MembershipBatchMapper;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoyaltyGradeQueryServiceTest {

    private LoyaltyGradeMapper mapper;
    private LoyaltyGradeRepository loyaltyGradeRepository;
    private MembershipBatchMapper membershipBatchMapper;
    private LoyaltyGradeQueryService service;

    @BeforeEach
    void setUp() {
        mapper = mock(LoyaltyGradeMapper.class);
        loyaltyGradeRepository = mock(LoyaltyGradeRepository.class);
        membershipBatchMapper = mock(MembershipBatchMapper.class);

        service = new LoyaltyGradeQueryService(mapper, loyaltyGradeRepository, membershipBatchMapper);
    }

    @Test
    @DisplayName("list: sortReq 세팅 후 mapper 호출, 결과 그대로 반환")
    void getList_success() {
        // given
        Long hotelGroupCode = 1L;
        String sortBy = "loyaltyTierLevel";
        String direction = "DESC";
        String status = "ACTIVE";

        List<LoyaltyGradeListQueryResponse> mockList = List.of(mock(LoyaltyGradeListQueryResponse.class));

        when(mapper.findLoyaltyGradeList(eq(hotelGroupCode), any(SortRequest.class), eq(status)))
                .thenReturn(mockList);

        // when
        List<LoyaltyGradeListQueryResponse> result = service.getLoyaltyGradeList(hotelGroupCode, sortBy, direction,
                status);

        // then
        assertThat(result).isSameAs(mockList);

        ArgumentCaptor<SortRequest> captor = ArgumentCaptor.forClass(SortRequest.class);
        verify(mapper).findLoyaltyGradeList(eq(hotelGroupCode), captor.capture(), eq(status));

        SortRequest sortReq = captor.getValue();
        assertThat(sortReq.getSortBy()).isEqualTo(sortBy);
        assertThat(sortReq.getDirection()).isEqualTo(direction);

        verifyNoMoreInteractions(mapper);
    }

    @Test
    @DisplayName("detail: 결과 null이면 LOYALTY_GRADE_NOT_FOUND")
    void getDetail_null_thenThrow() {
        // given
        Long hotelGroupCode = 1L;
        Long loyaltyGradeCode = 10L;
        when(mapper.findLoyaltyGradeDetail(hotelGroupCode, loyaltyGradeCode)).thenReturn(null);

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.getLoyaltyGradeDetail(hotelGroupCode, loyaltyGradeCode),
                CustomException.class);

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.LOYALTY_GRADE_NOT_FOUND);
        verify(mapper).findLoyaltyGradeDetail(hotelGroupCode, loyaltyGradeCode);
        verifyNoMoreInteractions(mapper);
    }

    @Test
    @DisplayName("detail: 정상 조회면 그대로 반환")
    void getDetail_success() {
        // given
        Long hotelGroupCode = 1L;
        Long loyaltyGradeCode = 10L;
        LoyaltyGradeDetailQueryResponse dto = mock(LoyaltyGradeDetailQueryResponse.class);
        when(mapper.findLoyaltyGradeDetail(hotelGroupCode, loyaltyGradeCode)).thenReturn(dto);

        // when
        LoyaltyGradeDetailQueryResponse result = service.getLoyaltyGradeDetail(hotelGroupCode, loyaltyGradeCode);

        // then
        assertThat(result).isSameAs(dto);
        verify(mapper).findLoyaltyGradeDetail(hotelGroupCode, loyaltyGradeCode);
        verifyNoMoreInteractions(mapper);
    }
}
