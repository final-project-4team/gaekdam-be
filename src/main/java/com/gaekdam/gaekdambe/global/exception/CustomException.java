package com.gaekdam.gaekdambe.global.exception;


import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

  private final ErrorCode errorCode;
  private final String customMessage;


  // 기본 ErrorCode 메시지 사용
  public CustomException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
    this.customMessage = null;
  }


  // ErrorCode + 커스텀 메시지 추가
  public CustomException(ErrorCode errorCode, String customMessage) {
    super(customMessage); // 부모 예외 메시지에 커스텀 메시지 저장
    this.errorCode = errorCode;
    this.customMessage = customMessage;
  }

}
