package com.gaekdam.gaekdambe.global.exception;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public enum ErrorCode {
  //1000번대는 공통 오류 처리
  UNAUTHORIZED_ACCESS("1000", "관리자만 접근 가능합니다", HttpStatus.UNAUTHORIZED),
  INVALID_REQUEST("1001", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
  INVALID_USER_ID("1002", "해당 사용자가 없습니다", HttpStatus.BAD_REQUEST),
  INVALID_ADMIN_ID("1003", "잘못된 관리자 ID입니다.", HttpStatus.BAD_REQUEST),
  INVALID_INCORRECT_FORMAT("1004", "잘못된 형식입니다.", HttpStatus.BAD_REQUEST),
  PASSWORD_NOT_MATCH("1005", "비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
  INVALID_INPUT_FORMAT("1006", "잘못된 입력 형식입니다.", HttpStatus.BAD_REQUEST),
  DUPLICATE_VALUE("1007", "중복된 값입니다.", HttpStatus.BAD_REQUEST),
  ALARM_NOT_FOUND("1008", "존재하지 않는 알람입니다.", HttpStatus.NOT_FOUND),
  NULL_UNAUTHORIZED("1009", "로그인 정보가 없습니다", HttpStatus.UNAUTHORIZED),

  UNAUTHORIZED("401","만료된 토큰" ,HttpStatus.UNAUTHORIZED ),

  RESERVATION_CONFLICT("4001", "선택하신 기간에 예약이 이미 존재합니다.", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatusCode httpStatusCode;


}
