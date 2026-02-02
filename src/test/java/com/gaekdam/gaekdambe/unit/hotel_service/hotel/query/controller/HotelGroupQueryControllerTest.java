package com.gaekdam.gaekdambe.unit.hotel_service.hotel.query.controller;

import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import com.gaekdam.gaekdambe.hotel_service.hotel.query.controller.HotelGroupQueryController;
import com.gaekdam.gaekdambe.hotel_service.hotel.query.dto.request.HotelGroupQueryRequest;
import com.gaekdam.gaekdambe.hotel_service.hotel.query.dto.response.HotelGroupListResponse;
import com.gaekdam.gaekdambe.hotel_service.hotel.query.service.HotelGroupQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HotelGroupQueryControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private HotelGroupQueryController controller;

    @Mock
    private HotelGroupQueryService queryService;
    @Mock
    private HotelGroupRepository repository;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("getHotelGroupList: 호텔 그룹 목록 조회 API 성공")
    void getHotelGroupList_success() throws Exception {
        // given
        HotelGroupListResponse item = new HotelGroupListResponse(100L, LocalDateTime.now(), "GroupName");
        PageResponse<HotelGroupListResponse> pageRes = new PageResponse<>(List.of(item), 1, 10, 1);
        given(queryService.getHotelGroupList(any(HotelGroupQueryRequest.class))).willReturn(pageRes);

        // when & then
        mockMvc.perform(get("/api/v1/hotel-group"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].hotelGroupName").value("GroupName"));
    }

    @Test
    @DisplayName("getHotelNameById: 호텔 그룹 이름 조회 API 성공")
    void getHotelNameById_success() throws Exception {
        // given
        Long hgCode = 100L;
        HotelGroup hg = HotelGroup.builder().hotelGroupCode(hgCode).hotelGroupName("GroupName").build();
        given(repository.findById(hgCode)).willReturn(Optional.of(hg));

        // when & then
        mockMvc.perform(get("/api/v1/hotel-group/{hotelGroupCode}", hgCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("GroupName"));
    }
}
