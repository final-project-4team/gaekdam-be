
package com.gaekdam.gaekdambe.iam_service.employee.command.application.controller;

import com.gaekdam.gaekdambe.global.config.jwt.JwtTokenProvider;
import com.gaekdam.gaekdambe.global.config.jwt.RefreshTokenService;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.crypto.SearchHashService;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.dto.request.EmployeeSecureRegistrationRequest;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.dto.request.EmployeeUpdateSecureRequest;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.dto.request.LoginRequest;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.dto.request.PasswordChangeRequest;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.dto.response.TokenResponse;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.service.EmployeeSecureRegistrationService;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.service.EmployeeUpdateService;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.EmployeeStatus;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import com.gaekdam.gaekdambe.iam_service.permission.command.infrastructure.PermissionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/employee")
public class EmployeeCommandController {
  private final PasswordEncoder passwordEncoder;
  private final EmployeeRepository employeeRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final PermissionRepository permissionRepository;
  private final RefreshTokenService redisRefreshTokenService;
  private final EmployeeSecureRegistrationService employeeSecureRegistrationService;
  private final EmployeeUpdateService employeeUpdateService;
  private final SearchHashService searchHashService;

  private static final String COOKIE_NAME = "refreshToken";
  private final long REFRESH_TOKEN_EXPIRE = 1000 * 60 * 60;

  //직원 추가
  @PostMapping("/add")
  public ResponseEntity<ApiResponse<String>> registerEmployee(@RequestBody EmployeeSecureRegistrationRequest request) {

    employeeSecureRegistrationService.registerEmployee(request);

    return ResponseEntity.ok(ApiResponse.success("유저 추가"));
  }

  //직원 로그인
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<TokenResponse>> login(
      @RequestBody LoginRequest request) {
    String loginId = request.loginId();
    String password = request.password();

    Employee employee = employeeRepository.findByLoginId(loginId)
        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_USER_ID));
    // 소프트 삭제된 계정(status = N) 체크
    if (employee.getEmployeeStatus() == EmployeeStatus.DORMANCY) {
      throw new CustomException(ErrorCode.INVALID_USER_ID, "휴면 처리된 회원 입니다.");
    }
    if (employee.getEmployeeStatus() == EmployeeStatus.LOCKED) {
      throw new CustomException(ErrorCode.INVALID_USER_ID, "이용 불가능 한 회원입니다.");
    }

    if (employee == null || !passwordEncoder.matches(password, employee.getPasswordHash())) {
      if (employee != null) {
        employeeSecureRegistrationService.loginFailed(employee);
      }
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponse.failure(
              ErrorCode.INVALID_USER_ID.getCode(),
              ErrorCode.INVALID_USER_ID.getMessage()));
    }

    // 로그인 성공 시 카운트 초기화 및 시간 갱신
    employeeSecureRegistrationService.loginSuccess(employee);

    String role = permissionRepository.findById(employee.getPermission().getPermissionCode()).get()
        .getPermissionName();

    String accessToken = jwtTokenProvider.createAccessToken(loginId, role);
    String refreshToken = jwtTokenProvider.createRefreshToken(loginId, role);

    // refreshToken을 Redis에 저장(회전 / 검증용)
    redisRefreshTokenService.save(loginId, refreshToken, REFRESH_TOKEN_EXPIRE);

    // 1) refreshToken을 HttpOnly 쿠키에 넣기
    ResponseCookie refreshCookie = ResponseCookie.from(COOKIE_NAME, refreshToken)
        .httpOnly(true)
        .secure(false) // 개발 환경: false, 배포시 true + https
        .path("/")
        .maxAge(REFRESH_TOKEN_EXPIRE / 1000) // 초 단위
        .sameSite("Lax")
        .build();

    // 2) body에는 accessToken만 내려주기
    TokenResponse body = TokenResponse.builder()
        .accessToken(accessToken)
        .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
        .body(ApiResponse.success(body));
  }

  //직원 정보 수정
  @PutMapping("/{employeeCode}")
  public ResponseEntity<ApiResponse<String>> updateEmployee(
      @PathVariable Long employeeCode,
      //정규식 검사
      @Valid @RequestBody EmployeeUpdateSecureRequest request) {
    employeeUpdateService.updateEmployee(employeeCode, request);
    return ResponseEntity.ok(ApiResponse.success("유저 정보 수정 완료"));
  }

  //직원 본인 비밀번호 변경
  @PatchMapping("/password")
  public ResponseEntity<ApiResponse<String>> changePassword(
      @AuthenticationPrincipal CustomUser customUser,
      @RequestBody PasswordChangeRequest request) {
    Employee employee =employeeRepository.findByLoginId(customUser.getUsername()).orElseThrow();
    employeeUpdateService.changePassword(employee, request);
    return ResponseEntity.ok(ApiResponse.success("비밀번호가 성공적으로 변경 되었습니다." ));
  }

  //직원 비밀번호 초기화
  @PatchMapping("/password-reset/{employeeCode}")
  public ResponseEntity<ApiResponse<String>> resetPassword(
      @PathVariable Long employeeCode) {
    String tempPassword=employeeUpdateService.resetPassword(employeeCode);
    return ResponseEntity.ok(ApiResponse.success("비밀번호가 성공적으로 초기화 되었습니다. 임시 비밀번호는 : "+tempPassword));
  }

  //직원 로그 아웃
  @DeleteMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(
      @AuthenticationPrincipal UserDetails userDetails,
      @CookieValue(name = COOKIE_NAME, required = false) String refreshToken) {
    if (userDetails != null) {
      String userId = userDetails.getUsername();
      redisRefreshTokenService.delete(userId);
    }

    ResponseCookie deleteCookie = ResponseCookie.from(COOKIE_NAME, "")
        .httpOnly(true)
        .secure(false) // 개발 환경에서는 false
        .path("/")
        .maxAge(0)
        .sameSite("Lax")
        .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
        .body(ApiResponse.success(null));
  }

  //직원 잠금 해제
  @PatchMapping("/unlock/{employeeCode}")
  public ResponseEntity<ApiResponse<String>> unlock(
      @PathVariable Long employeeCode
  ){

    employeeUpdateService.unlockEmployee(employeeCode);
    return ResponseEntity.ok(null);

  }


  //
  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
      @CookieValue(name = COOKIE_NAME, required = false) String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponse.failure("AUTH-001", "토큰이 없습니다."));
    }

    String userId = jwtTokenProvider.getUsername(refreshToken);

    if (!redisRefreshTokenService.isValid(userId, refreshToken)
        || !jwtTokenProvider.validateToken(refreshToken)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponse.failure("AUTH-002", "유효하지 않은 토큰입니다."));
    }

    String role = jwtTokenProvider.getRole(refreshToken);
    String newAccessToken = jwtTokenProvider.createAccessToken(userId, role);
    String newRefreshToken = jwtTokenProvider.createRefreshToken(userId, role);

    redisRefreshTokenService.save(userId, newRefreshToken, REFRESH_TOKEN_EXPIRE);

    ResponseCookie refreshCookie = ResponseCookie.from(COOKIE_NAME, newRefreshToken)
        .httpOnly(true)
        .secure(false)
        .path("/")
        .maxAge(REFRESH_TOKEN_EXPIRE / 1000)
        .sameSite("Lax")
        .build();

    TokenResponse body = TokenResponse.builder()
        .accessToken(newAccessToken)
        .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
        .body(ApiResponse.success(body));
  }

  @PostMapping("/check-duplicate-id")
  public ResponseEntity<ApiResponse<Boolean>> checkDuplicateId(@RequestBody Map<String, String> request) {
    String userId = request.get("userId");
    boolean exists = employeeRepository.existsByLoginId(userId);
    return ResponseEntity.ok(ApiResponse.success(exists));
  }

  @PostMapping("/check-duplicate-phone")
  public ResponseEntity<ApiResponse<Boolean>> checkDuplicatePhone(@RequestBody Map<String, String> request) {
    String phone = request.get("phone");
    byte[] phoneHash = searchHashService.phoneHash(phone);
    boolean exists = employeeRepository.existsByPhoneNumberHash(phoneHash);
    return ResponseEntity.ok(ApiResponse.success(exists));
  }


}
