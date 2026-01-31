package com.gaekdam.gaekdambe.global.crypto;

import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import java.util.regex.Pattern;
public class PasswordValidator {
  private static final String SPECIAL_CHARS_PATTERN = "[!@#$%^&*(),.?\":{}|<>]";
  private static final String NUMBER_PATTERN = ".*[0-9].*";
  private static final String UPPERCASE_PATTERN = ".*[A-Z].*";
  private static final String LOWERCASE_PATTERN = ".*[a-z].*";
  private static final String SPECIAL_CHAR_REGEX = ".*" + SPECIAL_CHARS_PATTERN + ".*";

  public static void validate(String password) {
    if (password == null || password.isBlank()) {
      throw new IllegalArgumentException("비밀번호는 필수 입력값입니다.");
    }
    int typeCount = 0;
    // 문자 종류 확인
    if (Pattern.matches(UPPERCASE_PATTERN, password)) typeCount++;
    if (Pattern.matches(LOWERCASE_PATTERN, password)) typeCount++;
    if (Pattern.matches(NUMBER_PATTERN, password)) typeCount++;
    if (Pattern.matches(SPECIAL_CHAR_REGEX, password)) typeCount++;
    // 조건 검사
    boolean isValid = false;

    //  3종류 이상 조합 -> 최소 8자리
    if (typeCount >= 3 && password.length() >= 8) {
      isValid = true;
    }
    //  2종류 이상 조합 -> 최소 10자리
    else if (typeCount >= 2 && password.length() >= 10) {
      isValid = true;
    }
    if (!isValid) {
      throw new CustomException(ErrorCode.PASSWORD_INCORRECT_FORMAT);
    }
  }
}