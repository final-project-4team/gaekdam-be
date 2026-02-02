package com.gaekdam.gaekdambe.unit.hotel_service.property.command.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.hotel_service.property.command.application.controller.PropertyCommandController;
import com.gaekdam.gaekdambe.hotel_service.property.command.application.dto.request.PropertyRequest;
import com.gaekdam.gaekdambe.hotel_service.property.command.application.service.PropertyCommandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PropertyCommandControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private PropertyCommandController controller;

    @Mock
    private PropertyCommandService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
    @DisplayName("createProperty: 지점 생성 성공")
    void createProperty_success() throws Exception {
        PropertyRequest req = new PropertyRequest("NewProp", "City");
        given(service.createProperty(any(PropertyRequest.class), eq(1L))).willReturn("지점 추가 성공");

        mockMvc.perform(post("/api/v1/property")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("지점 추가 성공"));
    }

    @Test
    @DisplayName("updateProperty: 지점 수정 성공")
    void updateProperty_success() throws Exception {
        Long pCode = 10L;
        PropertyRequest req = new PropertyRequest("UpdatedProp", "City");
        given(service.updateProperty(eq(pCode), any(PropertyRequest.class))).willReturn("수정 완료");

        mockMvc.perform(put("/api/v1/property/{propertyCode}", pCode)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("수정 완료"));
    }

    @Test
    @DisplayName("deleteProperty: 지점 삭제 성공")
    void deleteProperty_success() throws Exception {
        Long pCode = 10L;
        given(service.deleteProperty(pCode)).willReturn("삭제 완료");

        mockMvc.perform(delete("/api/v1/property/{propertyCode}", pCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("삭제 완료"));
    }
}
