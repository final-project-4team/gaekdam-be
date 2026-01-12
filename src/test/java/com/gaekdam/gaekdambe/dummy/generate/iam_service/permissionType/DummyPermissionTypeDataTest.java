package com.gaekdam.gaekdambe.dummy.generate.iam_service.permissionType;

import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.entity.PermissionType;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.infrastructure.PermissionTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DummyPermissionTypeDataTest {

  @Autowired
  private PermissionTypeRepository permissionTypeRepository;
  @Autowired
  private HotelGroupRepository hotelGroupRepository;

  @Transactional
  public void generate() {
    if (permissionTypeRepository.count() > 0) {
      return;
    }

    HotelGroup hotelGroup = hotelGroupRepository.findById(1L)
        .orElseThrow(() -> new IllegalArgumentException("HotelGroup with ID 1 not found"));

    for (PermissionTypeKey key : PermissionTypeKey.values()) {
      String keyName = key.name();
      int lastUnderscoreIndex = keyName.lastIndexOf('_');

      String resourceStr = (lastUnderscoreIndex != -1) ? keyName.substring(0, lastUnderscoreIndex) : keyName;
      String actionStr = (lastUnderscoreIndex != -1) ? keyName.substring(lastUnderscoreIndex + 1) : "";

      // 특수 케이스 처리 (DETAIL_READ, LIST_READ 등)
      if (keyName.endsWith("_DETAIL_READ")) {
        resourceStr = keyName.substring(0, keyName.indexOf("_DETAIL_READ"));
        actionStr = "DETAIL_READ";
      } else if (keyName.endsWith("_LIST_READ")) {
        resourceStr = keyName.substring(0, keyName.indexOf("_LIST_READ"));
        actionStr = "LIST_READ";
      } else if (keyName.endsWith("_DASHBOARD_CREATE")) {
        resourceStr = keyName.substring(0, keyName.indexOf("_DASHBOARD_CREATE"));
        actionStr = "DASHBOARD_CREATE";
      } else if (keyName.endsWith("_DASHBOARD_UPDATE")) {
        resourceStr = keyName.substring(0, keyName.indexOf("_DASHBOARD_UPDATE"));
        actionStr = "DASHBOARD_UPDATE";
      }

      String koreanResource = getKoreanResource(resourceStr);
      String koreanAction = getKoreanAction(actionStr);
      String permissionTypeName = koreanResource + " " + koreanAction;

      PermissionType permissionType = PermissionType.createPermissionType(
          key,
          permissionTypeName,
          resourceStr,
          actionStr,
          hotelGroup);

      permissionTypeRepository.save(permissionType);
    }
  }

  private String getKoreanResource(String resource) {
    return switch (resource) {
      case "REPORT" -> "리포트";
      case "MEMBER" -> "회원";
      case "EMPLOYEE" -> "직원";
      case "CUSTOMER" -> "고객";
      case "CUSTOMER_MEMO" -> "고객 메모";
      case "MEMBERSHIP_POLICY" -> "멤버십 정책";
      case "MEMBERSHIP" -> "멤버십";
      case "LOYALTY_POLICY" -> "로열티 정책";
      case "LOYALTY" -> "로열티";
      case "CUSTOMER_ACTIVITY" -> "고객 활동";
      case "CUSTOMER_TIMELINE" -> "고객 타임라인";
      case "CHECK_IN_OUT" -> "체크인/아웃";
      case "CHECK_IN" -> "체크인";
      case "CHECK_OUT" -> "체크아웃";
      case "FACILITY_USAGE" -> "시설 이용";
      case "INQUIRY" -> "문의";
      case "ACCIDENT" -> "사건사고";
      case "MESSAGE" -> "메시지";
      default -> resource;
    };
  }

  private String getKoreanAction(String action) {
    return switch (action) {
      case "CREATE" -> "생성";
      case "READ" -> "조회";
      case "UPDATE" -> "수정";
      case "DELETE" -> "삭제";
      case "DETAIL_READ" -> "상세 조회";
      case "LIST_READ" -> "목록 조회";
      case "DASHBOARD_CREATE" -> "대시보드 생성";
      case "DASHBOARD_UPDATE" -> "대시보드 수정";
      case "APPROVE" -> "승인";
      default -> action;
    };
  }
}
