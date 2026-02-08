package com.gaekdam.gaekdambe.unit.iam_service.employee.command.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.exception.GlobalExceptionHandler;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.controller.EmployeeCommandController;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.dto.request.EmployeeSecureRegistrationRequest;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.dto.request.EmployeeUpdateSecureRequest;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.dto.request.PasswordChangeRequest;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.service.EmployeeSecureRegistrationService;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.service.EmployeeUpdateService;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Import(GlobalExceptionHandler.class)
class EmployeeCommandControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private EmployeeCommandController controller;

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private EmployeeSecureRegistrationService registrationService;
    @Mock
    private EmployeeUpdateService updateService;

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
                .setControllerAdvice(new com.gaekdam.gaekdambe.global.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("registerEmployee: 직원 등록 성공")
    void registerEmployee_success() throws Exception {
        // given
        EmployeeSecureRegistrationRequest req = new EmployeeSecureRegistrationRequest(
                100L, "newId", "password", "email@test.com", "010-1234-5678", "Name", 1L, 2L, 3L, 4L);

        // when
        mockMvc.perform(post("/api/v1/employee/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("유저 추가"));

        // then
        verify(registrationService).registerEmployee(eq(1L), any(EmployeeSecureRegistrationRequest.class));
    }

    @Test
    @DisplayName("registerEmployee: 유효성 검사 실패 (400 Bad Request)")
    void registerEmployee_fail_validation() throws Exception {
        // given
        EmployeeSecureRegistrationRequest req = new EmployeeSecureRegistrationRequest(
                null, "", "", "invalid-email", "", "", null, null, null, null);

        // when & then
        mockMvc.perform(post("/api/v1/employee/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .   andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("updateEmployee: 직원 수정 성공")
    void updateEmployee_success() throws Exception {
        // given
        Long empCode = 10L;
        // Mock request with valid data (regex checks exist, so simple strings might
        // fail if invalid)
        // Adjusting regex compliant data if needed. Using generic valid-looking data.
        EmployeeUpdateSecureRequest req = new EmployeeUpdateSecureRequest(
                "010-9876-5432", "new@test.com", 101L, 201L, 301L,
                com.gaekdam.gaekdambe.iam_service.employee.command.domain.EmployeeStatus.ACTIVE);

        Employee accessor = mock(Employee.class);
        given(employeeRepository.findByLoginId("testAdmin")).willReturn(Optional.of(accessor));

        // when
        mockMvc.perform(put("/api/v1/employee/{employeeCode}", empCode)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("유저 정보 수정 완료"));

        // then
        verify(updateService).updateEmployee(eq(1L), eq(empCode), any(EmployeeUpdateSecureRequest.class), eq(accessor));
    }

    @Test
    @DisplayName("changePassword: 비밀번호 변경 성공")
    void changePassword_success() throws Exception {
        // given
        PasswordChangeRequest req = new PasswordChangeRequest("oldPass", "newPass");
        Employee employee = mock(Employee.class);
        given(employeeRepository.findByLoginId("testAdmin")).willReturn(Optional.of(employee));

        // when
        mockMvc.perform(patch("/api/v1/employee/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("비밀번호가 성공적으로 변경 되었습니다."));

        // then
        verify(updateService).changePassword(eq(employee), any(PasswordChangeRequest.class));
    }

    @Test
    @DisplayName("lockEmployee: 직원 잠금 성공")
    void lockEmployee_success() throws Exception {
        Long empCode = 10L;
        mockMvc.perform(patch("/api/v1/employee/lock/{employeeCode}", empCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("잠금 되었습니다"));

        verify(updateService).lockEmployee(1L, empCode);
    }

    @Test
    @DisplayName("unlockEmployee: 직원 잠금 해제 성공")
    void unlockEmployee_success() throws Exception {
        Long empCode = 10L;
        mockMvc.perform(patch("/api/v1/employee/unlock/{employeeCode}", empCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("잠금 상태가 해제 되었습니다"));

        verify(updateService).unlockEmployee(1L, empCode);
    }

    @Test
    @DisplayName("resetPassword: 비밀번호 초기화 성공")
    void resetPassword_success() throws Exception {
        Long empCode = 10L;
        given(updateService.resetPassword(empCode)).willReturn("tempPass123");

        mockMvc.perform(patch("/api/v1/employee/password-reset/{employeeCode}", empCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("비밀번호가 성공적으로 초기화 되었습니다.\n 임시 비밀번호는 : tempPass123"));
    }
}