package com.gaekdam.gaekdambe.global.config.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUserDetailsService;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final CustomUserDetailsService customUserDetailsService;
  private final RedisAccessTokenService redisAccessTokenService;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/actuator") || path.equals("/api/v1/auth/logout");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    try {

      if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
        filterChain.doFilter(request, response);
        return;
      }

      String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

      if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);
        return;
      }

      String token = authHeader.substring(7);

      jwtTokenProvider.validateTokenOrThrow(token);

      String username = jwtTokenProvider.getUsername(token);

      // Redis에 저장된 최신 토큰과 일치하는지 확인
      if (!redisAccessTokenService.isValid(username, token)) {
        throw new ExpiredJwtException(null, null, "다른 기기에서 로그인되어 만료된 토큰입니다.");
      }

      Long hotelGroupCode = jwtTokenProvider.getHotelGroupCode(token);
      Long propertyCode = jwtTokenProvider.getPropertyCode(token);

      var userDetails = customUserDetailsService.loadUserByUsername(username, hotelGroupCode, propertyCode);

      var authentication = new UsernamePasswordAuthenticationToken(
          userDetails,
          null,
          userDetails.getAuthorities());

      authentication.setDetails(
          new WebAuthenticationDetailsSource().buildDetails(request));

      SecurityContextHolder.getContext().setAuthentication(authentication);

      filterChain.doFilter(request, response);

    } catch (ExpiredJwtException e) {
      setErrorResponse(response, ErrorCode.UNAUTHORIZED, "토큰이 만료되었습니다.");
    } catch (MalformedJwtException e) {
      setErrorResponse(response, ErrorCode.INVALID_REQUEST, "유효하지 않은 토큰입니다.");
    } catch (JwtException e) {
      setErrorResponse(response, ErrorCode.INVALID_REQUEST, "잘못된 JWT 토큰입니다.");
    }
  }

  private void setErrorResponse(HttpServletResponse response,
      ErrorCode errorCode,
      String customMessage) throws IOException {

    response.setStatus(errorCode.getHttpStatusCode().value());
    response.setContentType("application/json;charset=UTF-8");

    ApiResponse<?> api = ApiResponse.failure(errorCode.name(), customMessage);

    ObjectMapper om = new ObjectMapper();// JAVA객체 -> JSON 혹은 JSON ->JAVA 객체(직렬화/역직렬화)
    om.registerModule(new JavaTimeModule()); // java.time 타입 처리할 수있게 등록
    om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);// 날짜를 배열 대신 문자열로 반환

    response.getWriter().write(om.writeValueAsString(api));// api객체 JSON으로 직렬화 후 문자열을 http응답 바디에 작성
  }
}
