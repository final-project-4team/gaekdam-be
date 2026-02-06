package com.gaekdam.gaekdambe.iam_service.permission.query.dto.response;


import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PermissionListResponse{
  private Long permissionCode;
  private String permissionName;
  //private Long hotelGroupCode;

  private List<PermissionTypeListResponse> permissionTypes = new ArrayList<>();
}
