package com.gaekdam.gaekdambe.unit.iam_service.log.query.controller;

import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.controller.LoginLogQueryController;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.request.LoginLogSearchRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.response.LoginLogQueryResponse;
import com.gaekdam.gaekdambe.iam_service.log.query.service.LoginLogQueryService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LoginLogQueryControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private LoginLogQueryController controller;

    @Mock
    private LoginLogQueryService queryService;

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
    @DisplayName("getLoginLogs: 로그인 로그 리스트 조회 성공")
    void getLoginLogs_success() throws Exception {
        // given
        LoginLogQueryResponse item = new LoginLogQueryResponse(
                1L, "LOGIN", 1L, "Name", "loginId", "1.1.1.1", null,
                com.gaekdam.gaekdambe.iam_service.log.command.domain.LoginResult.SUCCESS, null, 1L);
        PageResponse<LoginLogQueryResponse> pageRes = new PageResponse<>(List.of(item), 1, 10, 1);

        given(queryService.getLoginLogs(eq(1L), any(PageRequest.class), any(LoginLogSearchRequest.class),
                any(SortRequest.class))).willReturn(pageRes);

        // when & then
        mockMvc.perform(get("/api/v1/logs/login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].loginLogCode").value(1L));
    }
}
