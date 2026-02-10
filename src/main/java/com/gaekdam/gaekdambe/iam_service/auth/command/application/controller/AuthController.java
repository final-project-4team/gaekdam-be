package com.gaekdam.gaekdambe.iam_service.auth.command.application.controller;

import com.gaekdam.gaekdambe.global.Logging.IpLogging;
import com.gaekdam.gaekdambe.global.config.jwt.JwtTokenProvider;
import com.gaekdam.gaekdambe.global.config.jwt.RedisAccessTokenService;
import com.gaekdam.gaekdambe.global.config.jwt.RefreshTokenService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.crypto.SearchHashService;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.iam_service.auth.command.application.dto.request.LoginRequest;
import com.gaekdam.gaekdambe.iam_service.auth.command.application.dto.response.TokenResponse;
import com.gaekdam.gaekdambe.iam_service.auth.command.application.service.LoginAuthService;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.EmployeeStatus;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import com.gaekdam.gaekdambe.iam_service.permission.command.domain.entity.Permission;
import com.gaekdam.gaekdambe.iam_service.permission.command.infrastructure.PermissionRepository;
import com.gaekdam.gaekdambe.iam_service.permission_mapping.command.domain.entity.PermissionMapping;
import com.gaekdam.gaekdambe.iam_service.permission_mapping.command.infrastructure.PermissionMappingRepository;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
  private final PasswordEncoder passwordEncoder;
  private final EmployeeRepository employeeRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final PermissionRepository permissionRepository;
  private final RefreshTokenService redisRefreshTokenService;
  private final RedisAccessTokenService redisAccessTokenService;
  private final LoginAuthService loginAuthService;
  private final SearchHashService searchHashService;
  private final PermissionMappingRepository permissionMappingRepository;

  private static final String COOKIE_NAME = "refreshToken";
  private final long REFRESH_TOKEN_EXPIRE = 1000 * 60 * 60;
  private final IpLogging ipLogging;

  @Value("${app.cookie.secure}")
  private boolean cookieSecure;

  @Value("${app.cookie.same-site}")
  private String sameSite;

  // 직원 로그인
  @PostMapping("/login")
  @Operation(summary = "로그인", description = "직원은 로그인 할 수 있다.")
  public ResponseEntity<ApiResponse<TokenResponse>> login(
      @Parameter(description = "로그인 아이디,비밀번호") @RequestBody LoginRequest request) {
    String loginId = request.loginId();
    String password = request.password();
    String ip = ipLogging.searchIp();

    Employee employee = employeeRepository.findByLoginId(loginId)
        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_USER_ID));
    // 소프트 삭제된 계정(status = N) 체크
    if (employee.getEmployeeStatus() == EmployeeStatus.DORMANCY) {
      loginAuthService.loginFailed(employee, ip, "휴면 게정 접속 시도");
      throw new CustomException(ErrorCode.INVALID_USER_ID, "휴면 처리된 회원 입니다.");
    }
    if (employee.getEmployeeStatus() == EmployeeStatus.LOCKED) {
      loginAuthService.loginFailed(employee, ip, "잠긴 계정 접속 시도");
      throw new CustomException(ErrorCode.INVALID_USER_ID, "이용 불가능 한 회원입니다.");

    }

    if (!passwordEncoder.matches(password, employee.getPasswordHash())) {
      loginAuthService.loginFailed(employee, ip, "비밀번호 불일치");

      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponse.failure(
              ErrorCode.INVALID_USER_ID.getCode(),
              ErrorCode.INVALID_USER_ID.getMessage()));
    }

    // 로그인 성공 시 카운트 초기화 및 시간 갱신

    loginAuthService.loginSuccess(employee, ip);

    String role = permissionRepository.findById(employee.getPermission().getPermissionCode()).get()
        .getPermissionName();
    Long hotelGroupCode = employee.getHotelGroup().getHotelGroupCode();
    Long propertyCode = employee.getProperty().getPropertyCode();

    String accessToken = jwtTokenProvider.createAccessToken(employee.getLoginId(), role, hotelGroupCode,
        propertyCode);
    String refreshToken = jwtTokenProvider.createRefreshToken(employee.getLoginId(), role, hotelGroupCode,
        propertyCode);

    // refreshToken을 Redis에 저장(회전 / 검증용)

    redisAccessTokenService.save(employee.getLoginId(), accessToken);
    redisRefreshTokenService.save(employee.getLoginId(), refreshToken, REFRESH_TOKEN_EXPIRE);

    // 1) refreshToken을 HttpOnly 쿠키에 넣기
    ResponseCookie refreshCookie = ResponseCookie.from(COOKIE_NAME, refreshToken)
        .httpOnly(true)
        .secure(cookieSecure) // 개발 환경: false, 배포시 true + https
        .path("/")
        .maxAge(REFRESH_TOKEN_EXPIRE / 1000) // 초 단위
        .sameSite(sameSite)
        .build();

    // 2) body에는 accessToken만 내려주기
    TokenResponse body = TokenResponse.builder()
        .accessToken(accessToken)
        .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
        .body(ApiResponse.success(body));
  }

  @DeleteMapping("/logout")
  @Operation(summary = "로그아웃", description = "직원은 로그아웃 할 수 있다.")
  public ResponseEntity<ApiResponse<Void>> logout(
      @AuthenticationPrincipal UserDetails userDetails,
      @CookieValue(name = COOKIE_NAME, required = false) String refreshToken) {
    if (userDetails != null) {
      String userId = userDetails.getUsername();
      redisRefreshTokenService.delete(userId);
    }

    ResponseCookie deleteCookie = ResponseCookie.from(COOKIE_NAME, "")
        .httpOnly(true)
        .secure(cookieSecure) // 개발 환경에서는 false
        .path("/")
        .maxAge(0)
        .sameSite(sameSite)
        .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
        .body(ApiResponse.success(null));
  }

  //
  @PostMapping("/refresh")
  @Operation(summary = "refresh토큰 발급", description = "리프레시 토큰을 발급 받을 수 있다.")
  public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
      @CookieValue(name = COOKIE_NAME, required = false) String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponse.failure("AUTH-001", "토큰이 없습니다."));
    }

    String userId = jwtTokenProvider.getUsername(refreshToken);

    if (!redisRefreshTokenService.isValid(userId, refreshToken)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponse.failure("AUTH-002", "다른 기기에서 로그인되어 종료 합니다."));
    }

    if (!jwtTokenProvider.validateToken(refreshToken)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponse.failure("AUTH-002", "유효하지 않은 토큰입니다."));
    }

    String role = jwtTokenProvider.getRole(refreshToken);
    Long hotelGroupCode = jwtTokenProvider.getHotelGroupCode(refreshToken);
    Long propertyCode = jwtTokenProvider.getPropertyCode(refreshToken);

    String newAccessToken = jwtTokenProvider.createAccessToken(userId, role, hotelGroupCode,
        propertyCode);
    String newRefreshToken = jwtTokenProvider.createRefreshToken(userId, role, hotelGroupCode,
        propertyCode);

    redisAccessTokenService.save(userId, newAccessToken);
    redisRefreshTokenService.save(userId, newRefreshToken, REFRESH_TOKEN_EXPIRE);

    ResponseCookie refreshCookie = ResponseCookie.from(COOKIE_NAME, newRefreshToken)
        .httpOnly(true)
        .secure(cookieSecure)
        .path("/")
        .maxAge(REFRESH_TOKEN_EXPIRE / 1000)
        .sameSite(sameSite)
        .build();

    TokenResponse body = TokenResponse.builder()
        .accessToken(newAccessToken)
        .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
        .body(ApiResponse.success(body));
  }

  @PostMapping("/check-duplicate-id")
  public ResponseEntity<ApiResponse<Boolean>> checkDuplicateId(
      @RequestBody Map<String, String> request) {
    String userId = request.get("userId");
    boolean exists = employeeRepository.existsByLoginId(userId);
    return ResponseEntity.ok(ApiResponse.success(exists));
  }

  @PostMapping("/check-duplicate-phone")
  public ResponseEntity<ApiResponse<Boolean>> checkDuplicatePhone(
      @RequestBody Map<String, String> request) {
    String phone = request.get("phone");
    byte[] phoneHash = searchHashService.phoneHash(phone);
    boolean exists = employeeRepository.existsByPhoneNumberHash(phoneHash);
    return ResponseEntity.ok(ApiResponse.success(exists));
  }

  @GetMapping("/permissions")
  @Operation(summary = "권한 리스트 조회(프론트)", description = "직원 본인의 권한 리스트를 조회한다")
  public ResponseEntity<ApiResponse<List<PermissionTypeKey>>> getPermissions(
      @AuthenticationPrincipal UserDetails userDetails) {
    if (userDetails == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponse.failure("AUTH-006", "인증 정보가 없습니다."));
    }
    String userId = userDetails.getUsername();
    Employee employee = employeeRepository.findByLoginId(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_USER_ID));

    Permission permission = employee.getPermission();
    List<PermissionMapping> mappings = permissionMappingRepository.findAllByPermissionWithPermissionType(permission);
    List<PermissionTypeKey> keys = mappings.stream()
        .map(mapping -> mapping.getPermissionType().getPermissionTypeKey())
        .collect(Collectors.toList());

    return ResponseEntity.ok(ApiResponse.success(keys));
  }
}
