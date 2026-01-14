package com.gaekdam.gaekdambe.iam_service.auth.command.application.dto.request;


public record LoginRequest(
    String loginId,
    String password
) {
}