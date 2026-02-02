package com.gaekdam.gaekdambe.unit.hotel_service.position.query.service;

import com.gaekdam.gaekdambe.hotel_service.position.query.dto.response.HotelPositionListResponse;
import com.gaekdam.gaekdambe.hotel_service.position.query.mapper.HotelPositionQueryMapper;
import com.gaekdam.gaekdambe.hotel_service.position.query.service.HotelPositionQueryService;
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
class HotelPositionQueryServiceTest {

    @InjectMocks
    private HotelPositionQueryService service;

    @Mock
    private HotelPositionQueryMapper mapper;

    @Test
    @DisplayName("getHotelPositionList: 직급 목록 조회 성공")
    void getHotelPositionList_success() {
        // given
        Long hotelGroupCode = 1L;
        List<HotelPositionListResponse> mockList = List.of(
                new HotelPositionListResponse(10L, "Manager", 100L));
        given(mapper.findByHotelGroupCode(hotelGroupCode)).willReturn(mockList);

        // when
        List<HotelPositionListResponse> result = service.getHotelPositionList(hotelGroupCode);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).hotelPositionName()).isEqualTo("Manager");
    }
}
