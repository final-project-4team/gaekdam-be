package com.gaekdam.gaekdambe.unit.hotel_service.property.query.service;

import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.hotel_service.property.command.domain.PropertyStatus;
import com.gaekdam.gaekdambe.hotel_service.property.query.dto.request.PropertyQueryRequest;
import com.gaekdam.gaekdambe.hotel_service.property.query.dto.request.PropertySearchByHotelGroupRequest;
import com.gaekdam.gaekdambe.hotel_service.property.query.dto.request.PropertySearchRequest;
import com.gaekdam.gaekdambe.hotel_service.property.query.dto.response.PropertyListResponse;
import com.gaekdam.gaekdambe.hotel_service.property.query.mapper.PropertyMapper;
import com.gaekdam.gaekdambe.hotel_service.property.query.service.PropertyQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PropertyQueryServiceTest {

        @InjectMocks
        private PropertyQueryService service;

        @Mock
        private PropertyMapper mapper;

        @Test
        @DisplayName("getPropertyList: 지점 목록 조회 성공")
        void getPropertyList_success() {
                // given
                PropertyQueryRequest request = new PropertyQueryRequest(1, 10, 10L, "City", "Name",
                                PropertyStatus.ACTIVE, "Sort", "ASC");
                List<PropertyListResponse> mockList = List.of(
                                new PropertyListResponse(1L, "Seoul", "HotelSeoul", PropertyStatus.ACTIVE, "Group"));
                given(mapper.findPropertyList(any(PageRequest.class), any(PropertySearchRequest.class),
                                any(SortRequest.class)))
                                .willReturn(mockList);
                given(mapper.countPropertyList(any(PropertySearchRequest.class))).willReturn(1L);

                // when
                PageResponse<PropertyListResponse> result = service.getPropertyList(request);

                // then
                assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("getPropertiesByHotelGroup: 호텔 그룹별 지점 조회 성공")
        void getPropertiesByHotelGroup_success() {
                // given
                Long hgCode = 10L;
                List<PropertyListResponse> mockList = List.of(
                                new PropertyListResponse(1L, "Seoul", "HotelSeoul", PropertyStatus.ACTIVE, "Group"));
                given(mapper.findByHotelGroup(any(PropertySearchByHotelGroupRequest.class))).willReturn(mockList);

                // when
                List<PropertyListResponse> result = service.getPropertiesByHotelGroup(hgCode);

                // then
                assertThat(result).hasSize(1);
        }
}
