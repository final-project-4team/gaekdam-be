package com.gaekdam.gaekdambe.unit.iam_service.permission.command.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.iam_service.permission.command.application.controller.PermissionCommandController;
import com.gaekdam.gaekdambe.iam_service.permission.command.application.dto.request.PermissionCreateRequest;
import com.gaekdam.gaekdambe.iam_service.permission.command.application.dto.request.PermissionUpdateRequest;
import com.gaekdam.gaekdambe.iam_service.permission.command.application.service.PermissionCommandService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PermissionCommandControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private PermissionCommandController controller;

    @Mock
    private PermissionCommandService commandService;

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
    @DisplayName("createPermission: 권한 생성 요청 성공")
    void createPermission_success() throws Exception {
        // given
        PermissionCreateRequest req = new PermissionCreateRequest("NewPerm", List.of(1L, 2L));

        given(commandService.createPermission(any(PermissionCreateRequest.class), eq(1L))) // 1L from CustomUser
                .willReturn("권한 생성 완료");

        // when
        mockMvc.perform(post("/api/v1/permission")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("권한 생성 완료"));

        // then
        verify(commandService).createPermission(any(PermissionCreateRequest.class), eq(1L));
    }

    @Test
    @DisplayName("updatePermission: 권한 수정 요청 성공")
    void updatePermission_success() throws Exception {
        // given
        Long permCode = 10L;
        PermissionUpdateRequest req = new PermissionUpdateRequest(List.of(3L));

        given(commandService.updatePermission(eq(permCode), any(PermissionUpdateRequest.class), eq(1L),
                eq("testAdmin")))
                .willReturn("권한 수정 완료");

        // when
        mockMvc.perform(put("/api/v1/permission/{permissionCode}", permCode)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("권한 수정 완료"));
    }

    @Test
    @DisplayName("deletePermission: 권한 삭제 요청 성공")
    void deletePermission_success() throws Exception {
        // given
        Long permCode = 10L;

        given(commandService.deletePermission(eq(permCode), eq(1L)))
                .willReturn("권한 삭제");

        // when
        mockMvc.perform(delete("/api/v1/permission/{permissionCode}", permCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("권한 삭제"));
    }
}
