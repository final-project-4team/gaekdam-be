package com.gaekdam.gaekdambe.unit.hotel_service.position.query.controller;

import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.hotel_service.position.query.controller.HotelPositionQueryController;
import com.gaekdam.gaekdambe.hotel_service.position.query.dto.response.HotelPositionListResponse;
import com.gaekdam.gaekdambe.hotel_service.position.query.service.HotelPositionQueryService;
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

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HotelPositionQueryControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private HotelPositionQueryController controller;

    @Mock
    private HotelPositionQueryService service;

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
                        return new CustomUser("testAdmin", "pass", Collections.emptyList(), 1L, 2L);
                    }
                })
                .build();
    }

    @Test
    @DisplayName("getHotelPositionList: 직급 목록 조회 API 성공")
    void getHotelPositionList_success() throws Exception {
        // given
        List<HotelPositionListResponse> response = List.of(new HotelPositionListResponse(10L, "Manager", 100L));
        given(service.getHotelPositionList(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/position"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].hotelPositionName").value("Manager"));
    }
}
