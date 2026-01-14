package com.gaekdam.gaekdambe.iam_service.permission.command.application.dto.request;

import java.util.List;

public record PermissionCreateRequest (
    String permissionName,
    List<Long> permissionTypeList
    ){

}
