package com.gaekdam.gaekdambe.iam_service.permission.query.dto.request;

import java.util.List;

public record PermissionSearchRequest (
    String permissionName,
    List<String> resourceName
) {
}
