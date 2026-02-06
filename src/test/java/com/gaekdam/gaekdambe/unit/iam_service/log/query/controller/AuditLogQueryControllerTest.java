package com.gaekdam.gaekdambe.unit.iam_service.log.query.controller;

import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.controller.AuditLogQueryController;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.request.AuditLogSearchRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.response.AuditLogQueryResponse;
import com.gaekdam.gaekdambe.iam_service.log.query.service.AuditLogQueryService;
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
class AuditLogQueryControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private AuditLogQueryController controller;

    @Mock
    private AuditLogQueryService queryService;

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
    @DisplayName("getAuditLogs: 감사 로그 리스트 조회 성공")
    void getAuditLogs_success() throws Exception {
        // given
        AuditLogQueryResponse item = new AuditLogQueryResponse(
                1L, null, 1L, "admin", "Admin", 1L, "Details", "Old", "New", null);
        PageResponse<AuditLogQueryResponse> pageRes = new PageResponse<>(List.of(item), 1, 10, 1);

        given(queryService.getAuditLogs(eq(1L), any(PageRequest.class), any(AuditLogSearchRequest.class),
                any(SortRequest.class))).willReturn(pageRes);

        // when & then
        mockMvc.perform(get("/api/v1/logs/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].auditLogCode").value(1L));
    }

    @Test
    @DisplayName("getAuditLogDetail: 감사 로그 상세 조회 성공")
    void getAuditLogDetail_success() throws Exception {
        // given
        Long logCode = 100L;
        AuditLogQueryResponse response = new AuditLogQueryResponse(
                logCode, null, 1L, "admin", "Admin", 1L, "Details", "Old", "New", null);

        given(queryService.getAuditLog(logCode)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/logs/audit/{auditLogCode}", logCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.auditLogCode").value(logCode));
    }
}
