package com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds;

public enum PermissionTypeSeed {
  INQUIRY_READ("INQUIRY_READ","문의 조회", "INQUIRY", "READ"),
  INQUIRY_WRITE("STAY_READ", "투숙 조회", "STAY", "READ"),
  REPORT_VIEW("CUSTOMER_READ","고객 조회", "CUSTOMER", "READ"),
  USER_ADMIN("USER_CREATE","사용자 생성","USER", "CREATE");

  public final String permissionTypeKey;
  public final String permissionTypeName;
  public final String permissionTypeKeyResource;
  public final String permissionTypeAction;

  PermissionTypeSeed(String permissionTypeKey,String permissionTypeName, String permissionTypeKeyResource, String permissionTypeAction) {
    this.permissionTypeKey = permissionTypeKey;
    this.permissionTypeName = permissionTypeName;
    this.permissionTypeKeyResource = permissionTypeKeyResource;
    this.permissionTypeAction = permissionTypeAction;
  }
}
