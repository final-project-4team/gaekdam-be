package com.gaekdam.gaekdambe.global.exception;


import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * CustomException 처리
   */
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


  /**
   * Validation(@Valid) 오류 처리
   * - MethodArgumentNotValidException
   */
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


  /**
   * 그 외 모든 예외 처리
   * - NullPointerException
   * - IllegalArgumentException
   * - RuntimeException 등
   * 원래 메시지를 그대로 보내되, 서버 오류로 응답
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<?>> handleException(Exception ex) {

    return ResponseEntity
        .internalServerError()
        .body(ApiResponse.failure("INTERNAL_ERROR", ex.getMessage()));
  }
}
