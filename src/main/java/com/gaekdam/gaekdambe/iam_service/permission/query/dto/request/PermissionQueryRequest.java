package com.gaekdam.gaekdambe.iam_service.permission.query.dto.request;


import java.util.List;

public record PermissionQueryRequest(
    // Page
    Integer page,
    Integer size,
    // Search
    String permissionName,
    List<String> resourceName,
    // Sort
    String sortBy,
    String direction) {

  public PermissionQueryRequest {
    if (page == null)
      page = 1;
    if (size == null)
      size = 20;

    if (sortBy == null)
      sortBy = "permission_code";
    if (direction == null)
      direction = "ASC";
  }
}