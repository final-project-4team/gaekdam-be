package com.gaekdam.gaekdambe.unit.iam_service.auth.command.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaekdam.gaekdambe.global.Logging.IpLogging;
import com.gaekdam.gaekdambe.global.config.jwt.JwtTokenProvider;
import com.gaekdam.gaekdambe.global.config.jwt.RefreshTokenService;
import com.gaekdam.gaekdambe.global.crypto.SearchHashService;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.property.command.domain.entity.Property;
import com.gaekdam.gaekdambe.iam_service.auth.command.application.controller.AuthController;
import com.gaekdam.gaekdambe.iam_service.auth.command.application.dto.request.LoginRequest;
import com.gaekdam.gaekdambe.iam_service.auth.command.application.service.LoginAuthService;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.EmployeeStatus;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import com.gaekdam.gaekdambe.iam_service.permission.command.domain.entity.Permission;
import com.gaekdam.gaekdambe.iam_service.permission.command.infrastructure.PermissionRepository;
import com.gaekdam.gaekdambe.iam_service.permission_mapping.command.infrastructure.PermissionMappingRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

        private MockMvc mockMvc;

        @InjectMocks
        private AuthController authController;

        @Mock
        private PasswordEncoder passwordEncoder;
        @Mock
        private EmployeeRepository employeeRepository;
        @Mock
        private JwtTokenProvider jwtTokenProvider;
        @Mock
        private PermissionRepository permissionRepository;
        @Mock
        private RefreshTokenService redisRefreshTokenService;
        @Mock
        private LoginAuthService loginAuthService;
        @Mock
        private SearchHashService searchHashService;
        @Mock
        private com.gaekdam.gaekdambe.global.config.jwt.RedisAccessTokenService redisAccessTokenService;
        @Mock
        private PermissionMappingRepository permissionMappingRepository;
        @Mock
        private IpLogging ipLogging;

        private ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setup() {
                mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        }

        @Test
        @DisplayName("login: 정상 로그인 성공")
        void login_success() throws Exception {
                // given
                LoginRequest req = new LoginRequest("user", "pass");

                Employee employee = org.mockito.Mockito.mock(Employee.class);
                HotelGroup hg = org.mockito.Mockito.mock(HotelGroup.class);
                Property prop = org.mockito.Mockito.mock(Property.class);
                Permission perm = org.mockito.Mockito.mock(Permission.class);

                given(ipLogging.searchIp()).willReturn("127.0.0.1");
                given(employeeRepository.findByLoginId("user")).willReturn(Optional.of(employee));
                given(employee.getEmployeeStatus()).willReturn(EmployeeStatus.ACTIVE);
                given(employee.getPasswordHash()).willReturn("encodedPass");
                given(passwordEncoder.matches("pass", "encodedPass")).willReturn(true);

                given(employee.getHotelGroup()).willReturn(hg);
                given(hg.getHotelGroupCode()).willReturn(1L);
                given(employee.getProperty()).willReturn(prop);
                given(prop.getPropertyCode()).willReturn(2L);
                given(employee.getPermission()).willReturn(perm);
                given(perm.getPermissionCode()).willReturn(10L);

                given(permissionRepository.findById(10L)).willReturn(Optional.of(perm));
                given(perm.getPermissionName()).willReturn("ADMIN");

                given(jwtTokenProvider.createAccessToken(anyString(), anyString(), anyLong(), anyLong()))
                                .willReturn("access-token");
                given(jwtTokenProvider.createRefreshToken(anyString(), anyString(), anyLong(), anyLong()))
                                .willReturn("refresh-token");

                // when
                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                                .andExpect(header().exists("Set-Cookie"));

                // then
                verify(loginAuthService).loginSuccess(any(), anyString());
                verify(redisRefreshTokenService).save(anyString(), anyString(), anyLong());
        }

        @Test
        @DisplayName("login: 비밀번호 불일치 실패")
        void login_fail_password() throws Exception {
                // given
                LoginRequest req = new LoginRequest("user", "wrong");
                Employee employee = org.mockito.Mockito.mock(Employee.class);

                given(ipLogging.searchIp()).willReturn("127.0.0.1");
                given(employeeRepository.findByLoginId("user")).willReturn(Optional.of(employee));
                given(employee.getEmployeeStatus()).willReturn(EmployeeStatus.ACTIVE);
                given(employee.getPasswordHash()).willReturn("encodedPass");
                given(passwordEncoder.matches("wrong", "encodedPass")).willReturn(false);

                // when
                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isUnauthorized());

                // then
                verify(loginAuthService).loginFailed(any(), anyString(), anyString());
        }

        @Test
        @DisplayName("refresh: 리프레시 토큰으로 재발급 성공")
        void refresh_success() throws Exception {
                // given
                String refreshToken = "valid-refresh-token";
                Cookie cookie = new Cookie("refreshToken", refreshToken);

                given(jwtTokenProvider.getUsername(refreshToken)).willReturn("user");
                given(redisRefreshTokenService.isValid("user", refreshToken)).willReturn(true);
                given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);

                given(jwtTokenProvider.getRole(refreshToken)).willReturn("ADMIN");
                given(jwtTokenProvider.getHotelGroupCode(refreshToken)).willReturn(1L);
                given(jwtTokenProvider.getPropertyCode(refreshToken)).willReturn(2L);

                given(jwtTokenProvider.createAccessToken(anyString(), anyString(), anyLong(), anyLong()))
                                .willReturn("new-access");
                given(jwtTokenProvider.createRefreshToken(anyString(), anyString(), anyLong(), anyLong()))
                                .willReturn("new-refresh");

                // when
                mockMvc.perform(post("/api/v1/auth/refresh")
                                .cookie(cookie))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.accessToken").value("new-access"));

                // then
                verify(redisRefreshTokenService).save(anyString(), anyString(), anyLong());
        }
}
