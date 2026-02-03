package com.gaekdam.gaekdambe.unit.customer_service.membership.query.service;

import com.gaekdam.gaekdambe.customer_service.membership.query.dto.response.MembershipGradeDetailQueryResponse;
import com.gaekdam.gaekdambe.customer_service.membership.query.dto.response.MembershipGradeListQueryResponse;
import com.gaekdam.gaekdambe.customer_service.membership.query.mapper.MembershipGradeMapper;
import com.gaekdam.gaekdambe.customer_service.membership.query.service.MembershipGradeQueryService;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembershipGradeQueryServiceTest {

    private MembershipGradeMapper mapper;
    private MembershipGradeQueryService service;

    @BeforeEach
    void setUp() {
        mapper = mock(MembershipGradeMapper.class);
        service = new MembershipGradeQueryService(mapper);
    }

    @Test
    @DisplayName("list: 매퍼 결과 그대로 반환")
    void getList_success() {
        // given
        Long hotelGroupCode = 1L;
        SortRequest sort = new SortRequest();
        sort.setSortBy("tierLevel");
        sort.setDirection("DESC");
        String status = "ALL";

        List<MembershipGradeListQueryResponse> mockList = List.of(
                new MembershipGradeListQueryResponse(10L, "GOLD", 2L, 1000L, 1, 12, 1)
        );
        when(mapper.findMembershipGradeList(hotelGroupCode, sort, status)).thenReturn(mockList);

        // when
        List<MembershipGradeListQueryResponse> result =
                service.getMembershipGradeList(hotelGroupCode, sort, status);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).gradeName()).isEqualTo("GOLD");
        verify(mapper).findMembershipGradeList(hotelGroupCode, sort, status);
        verifyNoMoreInteractions(mapper);
    }

    @Test
    @DisplayName("detail: 결과 null이면 MEMBERSHIP_GRADE_NOT_FOUND")
    void getDetail_null_thenThrow() {
        // given
        Long hotelGroupCode = 1L;
        Long gradeCode = 10L;
        when(mapper.findMembershipGradeDetail(hotelGroupCode, gradeCode)).thenReturn(null);

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.getMembershipGradeDetail(hotelGroupCode, gradeCode),
                CustomException.class
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBERSHIP_GRADE_NOT_FOUND);
        verify(mapper).findMembershipGradeDetail(hotelGroupCode, gradeCode);
        verifyNoMoreInteractions(mapper);
    }

    @Test
    @DisplayName("detail: 정상 조회면 그대로 반환")
    void getDetail_success() {
        // given
        Long hotelGroupCode = 1L;
        Long gradeCode = 10L;

        MembershipGradeDetailQueryResponse dto = new MembershipGradeDetailQueryResponse(
                gradeCode, "GOLD", 2L, "tier", 1000L, 1, 12, 1,
                null, null
        );
        when(mapper.findMembershipGradeDetail(hotelGroupCode, gradeCode)).thenReturn(dto);

        // when
        MembershipGradeDetailQueryResponse result =
                service.getMembershipGradeDetail(hotelGroupCode, gradeCode);

        // then
        assertThat(result).isSameAs(dto);
        verify(mapper).findMembershipGradeDetail(hotelGroupCode, gradeCode);
        verifyNoMoreInteractions(mapper);
    }
}
