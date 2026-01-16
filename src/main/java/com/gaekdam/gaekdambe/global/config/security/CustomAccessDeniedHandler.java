package com.gaekdam.gaekdambe.global.config.security;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  @Override
  public void handle(HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException {

    response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
    response.setContentType("application/json;charset=UTF-8");

    ApiResponse<?> body = ApiResponse.failure(ErrorCode.UNAUTHORIZED_ACCESS.getCode(),"접근권한이 없습니다");

    response.getWriter().write(new ObjectMapper().writeValueAsString(body));
  }
}
