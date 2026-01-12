package com.gaekdam.gaekdambe.iam_service.employee.command.application.dto.request;

public record PasswordChangeRequest(
        String currentPassword,
        String newPassword) {
}
