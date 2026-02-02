package com.gaekdam.gaekdambe.unit.hotel_service.hotel.query.service;

import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.hotel_service.hotel.query.dto.request.HotelGroupQueryRequest;
import com.gaekdam.gaekdambe.hotel_service.hotel.query.dto.request.HotelGroupSearchRequest;
import com.gaekdam.gaekdambe.hotel_service.hotel.query.dto.response.HotelGroupListResponse;
import com.gaekdam.gaekdambe.hotel_service.hotel.query.mapper.HotelGroupMapper;
import com.gaekdam.gaekdambe.hotel_service.hotel.query.service.HotelGroupQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HotelGroupQueryServiceTest {

    @InjectMocks
    private HotelGroupQueryService service;

    @Mock
    private HotelGroupMapper mapper;

    @Test
    @DisplayName("getHotelGroupList: 호텔 그룹 목록 조회 성공")
    void getHotelGroupList_success() {
        // given
        HotelGroupQueryRequest request = new HotelGroupQueryRequest(1, 10, 100L, "GroupName", "name", "ASC");
        List<HotelGroupListResponse> mockList = List.of(
                new HotelGroupListResponse(100L, LocalDateTime.now(), "GroupName"));
        given(mapper.findHotelGroupList(any(PageRequest.class), any(HotelGroupSearchRequest.class),
                any(SortRequest.class)))
                .willReturn(mockList);
        given(mapper.countHotelGroupList(any(HotelGroupSearchRequest.class))).willReturn(1L);

        // when
        PageResponse<HotelGroupListResponse> result = service.getHotelGroupList(request);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).hotelGroupName()).isEqualTo("GroupName");
        verify(mapper).findHotelGroupList(any(PageRequest.class), any(HotelGroupSearchRequest.class),
                any(SortRequest.class));
    }
}
