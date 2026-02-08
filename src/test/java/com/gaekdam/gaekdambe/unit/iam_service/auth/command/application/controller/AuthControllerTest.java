package com.gaekdam.gaekdambe.unit.iam_service.auth.command.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaekdam.gaekdambe.global.Logging.IpLogging;
import com.gaekdam.gaekdambe.global.config.jwt.JwtTokenProvider;
import com.gaekdam.gaekdambe.global.config.jwt.RedisAccessTokenService;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private AuthController authController;

    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private PermissionRepository permissionRepository;

    @Mock private RefreshTokenService redisRefreshTokenService;
    @Mock private RedisAccessTokenService redisAccessTokenService;

    @Mock private LoginAuthService loginAuthService; // ✅ 이거 없으면 NPE 터짐
    @Mock private SearchHashService searchHashService;

    @Mock private PermissionMappingRepository permissionMappingRepository;
    @Mock private IpLogging ipLogging;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    @DisplayName("login: 정상 로그인 성공")
    void login_success() throws Exception {
        LoginRequest req = new LoginRequest("user", "pass");

        Employee employee = Mockito.mock(Employee.class);
        HotelGroup hg = Mockito.mock(HotelGroup.class);
        Property prop = Mockito.mock(Property.class);
        Permission perm = Mockito.mock(Permission.class);

        given(ipLogging.searchIp()).willReturn("127.0.0.1");
        given(employeeRepository.findByLoginId("user")).willReturn(Optional.of(employee));
        given(employee.getEmployeeStatus()).willReturn(EmployeeStatus.ACTIVE);
        given(employee.getPasswordHash()).willReturn("encodedPass");
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);

        given(employee.getHotelGroup()).willReturn(hg);
        given(hg.getHotelGroupCode()).willReturn(1L);
        given(employee.getProperty()).willReturn(prop);
        given(prop.getPropertyCode()).willReturn(2L);
        given(employee.getPermission()).willReturn(perm);
        given(perm.getPermissionCode()).willReturn(10L);

        given(permissionRepository.findById(anyLong())).willReturn(Optional.of(perm));
        given(perm.getPermissionName()).willReturn("ADMIN");

        given(jwtTokenProvider.createAccessToken(any(), any(), anyLong(), anyLong()))
                .willReturn("access-token");
        given(jwtTokenProvider.createRefreshToken(any(), any(), anyLong(), anyLong()))
                .willReturn("refresh-token");

        // ✅ void side-effect는 막아두는 게 안전
        doNothing().when(redisAccessTokenService).save(anyString(), anyString());
        doNothing().when(redisRefreshTokenService).save(anyString(), anyString(), anyLong());
        doNothing().when(loginAuthService).loginSuccess(any(), anyString());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..accessToken").exists());
    }
}
