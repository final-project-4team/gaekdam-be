package com.gaekdam.gaekdambe.iam_service.auth.command.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TokenResponse {
  private String accessToken;
  private String refreshToken;
}
