package com.gaekdam.gaekdambe.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import org.springframework.security.access.AccessDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  //CustomException 처리
  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ApiResponse<?>> handleCustomException(CustomException e) {

    ErrorCode errorCode = e.getErrorCode();

    String message = (e.getCustomMessage() != null)
        ? e.getCustomMessage()
        : errorCode.getMessage();

    return ResponseEntity
        .status(errorCode.getHttpStatusCode().value())
        .body(ApiResponse.failure(errorCode.name(), message));
  }

  // Validation(@Valid) 오류 처리
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException ex) {

    String message = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .findFirst()
        .map(error -> error.getDefaultMessage())
        .orElse("잘못된 요청입니다.");

    return ResponseEntity
        .badRequest()
        .body(ApiResponse.failure("INVALID_REQUEST", message));
  }

  // 그 외 모든 예외 처리
  // 서버 오류로 응답

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<?>> handleException(Exception ex) {

    return ResponseEntity
        .internalServerError()
        .body(ApiResponse.failure("INTERNAL_ERROR", ex.getMessage()));
  }

  //권한 없을 때 (Security)
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException ex) {
    return ResponseEntity
        .status(org.springframework.http.HttpStatus.FORBIDDEN)
        .body(ApiResponse.failure(ErrorCode.UNAUTHORIZED_ACCESS.getCode(), "접근권한이 없습니다"));
  }
}
