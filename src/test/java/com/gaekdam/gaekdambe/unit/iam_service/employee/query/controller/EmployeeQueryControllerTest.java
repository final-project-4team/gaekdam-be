package com.gaekdam.gaekdambe.unit.iam_service.employee.query.controller;

import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.employee.query.controller.EmployeeQueryController;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.request.EmployeeQuerySearchRequest;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeDetailResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeListResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.service.EmployeeQueryService;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EmployeeQueryControllerTest {

        private MockMvc mockMvc;

        @InjectMocks
        private EmployeeQueryController controller;

        @Mock
        private EmployeeQueryService queryService;

        @BeforeEach
        void setup() {
                mockMvc = MockMvcBuilders.standaloneSetup(controller)
                                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                                        @Override
                                        public boolean supportsParameter(MethodParameter parameter) {
                                                return parameter.getParameterType().isAssignableFrom(CustomUser.class)
                                                                || parameter.hasParameterAnnotation(
                                                                                AuthenticationPrincipal.class);
                                        }

                                        @Override
                                        public Object resolveArgument(MethodParameter parameter,
                                                        ModelAndViewContainer mavContainer,
                                                        NativeWebRequest webRequest, WebDataBinderFactory binderFactory)
                                                        throws Exception {
                                                return new CustomUser("testAdmin", "pass", Collections.emptyList(), 1L,
                                                                2L);
                                        }
                                })
                                .build();
        }

        @Test
        @DisplayName("getEmployee: 직원 상세 조회 성공")
        void getEmployee_success() throws Exception {
                // given
                Long targetEmpCode = 10L;
                String reason = "check";
                EmployeeDetailResponse response = new EmployeeDetailResponse(
                                targetEmpCode, 1001L, "loginId", "Name", "phone", "email",
                                "Dept", "Pos", "Prop", "HG", "Perm",
                                1L, 2L, 3L, 1L, 4L,
                                LocalDateTime.now(), "ACTIVE", LocalDateTime.now(), LocalDateTime.now(), 0,
                                LocalDateTime.now());

                given(queryService.getEmployeeDetail(eq(1L), eq(targetEmpCode), eq(reason))).willReturn(response);

                // when & then
                mockMvc.perform(get("/api/v1/employee/detail/{employeeCode}", targetEmpCode)
                                .param("reason", reason))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.employeeCode").value(targetEmpCode));
        }

        @Test
        @DisplayName("searchEmployee: 직원 리스트 조회 성공")
        void searchEmployee_success() throws Exception {
                // given
                EmployeeListResponse item = new EmployeeListResponse(
                                10L, 1001L, "Perm", "Name", "phone", "email", "loginId",
                                com.gaekdam.gaekdambe.iam_service.employee.command.domain.EmployeeStatus.ACTIVE);
                PageResponse<EmployeeListResponse> pageRes = new PageResponse<>(List.of(item), 1, 10, 1);

                given(queryService.searchEmployees(eq(1L), any(EmployeeQuerySearchRequest.class),
                                any(PageRequest.class),
                                any(SortRequest.class))).willReturn(pageRes);

                // when & then
                mockMvc.perform(get("/api/v1/employee")
                                .param("page", "1")
                                .param("size", "10")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.content[0].employeeCode").value(10L));
        }

        @Test
        @DisplayName("getMyPage: 내 정보 조회 성공")
        void getMyPage_success() throws Exception {
                // given
                EmployeeDetailResponse response = new EmployeeDetailResponse(
                                2L, 1002L, "testAdmin", "Name", "phone", "email",
                                "Dept", "Pos", "Prop", "HG", "Perm",
                                1L, 2L, 3L, 1L, 4L,
                                LocalDateTime.now(), "ACTIVE", LocalDateTime.now(), LocalDateTime.now(), 0,
                                LocalDateTime.now());

                given(queryService.getMyPage(eq(1L), eq("testAdmin"))).willReturn(response);

                // when & then
                mockMvc.perform(get("/api/v1/employee/detail"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.loginId").value("testAdmin"));
        }
}
