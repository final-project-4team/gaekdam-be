package com.gaekdam.gaekdambe.unit.hotel_service.property.query.controller;

import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import com.gaekdam.gaekdambe.hotel_service.property.command.domain.PropertyStatus;
import com.gaekdam.gaekdambe.hotel_service.property.command.domain.entity.Property;
import com.gaekdam.gaekdambe.hotel_service.property.command.infrastructure.PropertyRepository;
import com.gaekdam.gaekdambe.hotel_service.property.query.controller.PropertyQueryController;
import com.gaekdam.gaekdambe.hotel_service.property.query.dto.request.PropertyQueryRequest;
import com.gaekdam.gaekdambe.hotel_service.property.query.dto.response.PropertyListResponse;
import com.gaekdam.gaekdambe.hotel_service.property.query.service.PropertyQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PropertyQueryControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private PropertyQueryController controller;

    @Mock
    private PropertyQueryService queryService;
    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private HotelGroupRepository hotelGroupRepository;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType().isAssignableFrom(CustomUser.class)
                                || parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
                        // CustomUser contains propertyCode=2L, hotelGroupCode=1L
                        return new CustomUser("testAdmin", "pass", Collections.emptyList(), 1L, 2L);
                    }
                })
                .build();
    }

    @Test
    @DisplayName("getPropertyList: 지점 목록 조회 성공")
    void getPropertyList_success() throws Exception {
        PropertyListResponse item = new PropertyListResponse(1L, "Seoul", "Hotel", PropertyStatus.ACTIVE, "GR");
        PageResponse<PropertyListResponse> pageRes = new PageResponse<>(List.of(item), 1, 10, 1);
        given(queryService.getPropertyList(any(PropertyQueryRequest.class))).willReturn(pageRes);

        mockMvc.perform(get("/api/v1/property"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].propertyName").value("Hotel"));
    }

    @Test
    @DisplayName("getMyProperty: 내 지점 조회 성공")
    void getMyProperty_success() throws Exception {
        // CustomUser(1L, 2L) -> propertyCode=2L, hotelGroupCode=1L
        Long propCode = 2L;
        Long hgCode = 1L;

        Property prop = Property.builder().propertyCode(propCode).propertyName("MyProb").build();
        HotelGroup hg = HotelGroup.builder().hotelGroupCode(hgCode).hotelGroupName("MyHG").build();

        given(propertyRepository.findById(propCode)).willReturn(Optional.of(prop));
        given(hotelGroupRepository.findById(hgCode)).willReturn(Optional.of(hg));

        mockMvc.perform(get("/api/v1/property/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.propertyName").value("MyProb"));
    }

    @Test
    @DisplayName("getPropertyByHotelGroup: 호텔 그룹별 지점 조회 성공")
    void getPropertyByHotelGroup_success() throws Exception {
        PropertyListResponse item = new PropertyListResponse(1L, "Seoul", "Hotel", PropertyStatus.ACTIVE, "GR");
        given(queryService.getPropertiesByHotelGroup(1L)).willReturn(List.of(item));

        mockMvc.perform(get("/api/v1/property/by-hotel-group"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].propertyName").value("Hotel"));
    }
}
