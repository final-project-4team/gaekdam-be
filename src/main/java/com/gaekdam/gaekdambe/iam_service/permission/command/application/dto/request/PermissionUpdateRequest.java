package com.gaekdam.gaekdambe.iam_service.permission.command.application.dto.request;

import java.util.List;

public record PermissionUpdateRequest (
    List<Long> permissionTypeList
){

}
