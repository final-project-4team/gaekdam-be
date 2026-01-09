package com.gaekdam.gaekdambe.dummy.iam_service.permission;

import com.gaekdam.gaekdambe.iam_service.permission.command.domain.entity.Permission;
import com.gaekdam.gaekdambe.iam_service.permission.command.infrastructure.PermissionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Rollback(false)
public class DummyPermissionDataTest {
  @Autowired
  private PermissionRepository permissionRepository;

  @Test
  @DisplayName("권한 생성")
  void createPermissions()
  {
    for(long hotel=1;hotel<=10;hotel++) {
/*      Object[][] permissionsDummy={
          {"경영-총지배인",hotel},
          {"경영-부지배인",hotel},
          {"경영-지점장",hotel},
          {"지원-회계부장",hotel},
          {"지원-회계부사원",hotel},
          {"객실-하우스 키핑 매니저",hotel},
          {"객실-청소 직원",hotel},
          {"식음-레스토랑 매니저",hotel},
          {"식음-연회 매니저",hotel},
          {"식음-서빙",hotel},
          {"조리-중방장",hotel},
          {"조리-조리사",hotel},
          {"세일즈-세일즈 매니저",hotel},
          {"세일즈-세일즈 디렉터",hotel},
          {"홍보-마케팅 매니저",hotel},
          {"홍보-브랜드 매니저",hotel},
          {"홍보-시설 팀장",hotel},
          {"홍보-난방 기사",hotel},
      };*/
      String []permissionDummy=
      {   "경영-총지배인",
          "경영-부지배인",
          "경영-지점장",
          "지원-회계부장",
          "지원-회계부사원",
          "객실-하우스 키핑 매니저",
          "객실-청소 직원",
          "식음-레스토랑 매니저",
          "식음-연회 매니저",
          "식음-서빙",
          "조리-중방장",
          "조리-조리사",
          "세일즈-세일즈 매니저",
          "세일즈-세일즈 디렉터",
          "홍보-마케팅 매니저",
          "홍보-브랜드 매니저",
          "홍보-시설 팀장",
          "홍보-난방 기사"};
      for(String permissions:permissionDummy) {
      Permission permission = Permission.createPermission(
          permissions,hotel
        );
        permissionRepository.save(permission);
      }
    }
  }
}
