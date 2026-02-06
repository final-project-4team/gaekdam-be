package com.gaekdam.gaekdambe.unit.hotel_service.department.query.service;

import com.gaekdam.gaekdambe.hotel_service.department.query.dto.response.DepartmentListResponse;
import com.gaekdam.gaekdambe.hotel_service.department.query.mapper.DepartmentMapper;
import com.gaekdam.gaekdambe.hotel_service.department.query.service.DepartmentQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DepartmentQueryServiceTest {

    @InjectMocks
    private DepartmentQueryService service;

    @Mock
    private DepartmentMapper mapper;

    @Test
    @DisplayName("getDepartmentList: 부서 목록 조회 성공")
    void getDepartmentList_success() {
        // given
        Long hotelGroupCode = 1L;
        List<DepartmentListResponse> expectedList = List.of(
                new DepartmentListResponse(10L, "HR"),
                new DepartmentListResponse(20L, "IT"));
        given(mapper.findDepartmentList(hotelGroupCode)).willReturn(expectedList);

        // when
        List<DepartmentListResponse> result = service.getDepartmentList(hotelGroupCode);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).departmentName()).isEqualTo("HR");
    }
}
