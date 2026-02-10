package com.gaekdam.gaekdambe.dummy.generate.iam_service.permission;

import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import com.gaekdam.gaekdambe.iam_service.permission.command.domain.entity.Permission;
import com.gaekdam.gaekdambe.iam_service.permission.command.infrastructure.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DummyPermissionDataTest {
  @Autowired
  private PermissionRepository permissionRepository;

  @Autowired
  private HotelGroupRepository hotelGroupRepository;

 @Transactional
  public void generate()
  {

    if (permissionRepository.count() > 0) {
      return;
    }

    permissionRepository.save(
        Permission.createPermission(
            "초기화 된 권한",
            hotelGroupRepository.findById(10L).orElseThrow()
        )
    );

    for(long hotel=1;hotel<=5;hotel++) {


      String []permissionDummy=
      {
          "경영-총지배인",
          "경영-부지배인",
          "지원-회계부장",
          "지원-회계부사원",
          "객실-하우스 키핑 매니저",
          "객실-청소 직원",
          "식음-레스토랑 매니저",
          "식음-연회 매니저",
          "조리-주방장",
          "조리-조리사",
          "세일즈-세일즈 매니저",
          "세일즈-세일즈 디렉터",
          "홍보-마케팅 매니저",
          "홍보-브랜드 매니저",
          "시설-시설 팀장",
          "시설-난방 기사"
      };
      for(String permissions:permissionDummy) {
      Permission permission = Permission.createPermission(
          permissions,
          hotelGroupRepository.findById(hotel).orElseThrow()
        );
        permissionRepository.save(permission);
      }
    }
  }
}
