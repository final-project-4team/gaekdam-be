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
      case "REPORT_DASHBOARD" -> "리포트 대시보드";
      case "MEMBER" -> "회원";
      case "EMPLOYEE" -> "직원";
      case "CUSTOMER" -> "고객";
      case "CUSTOMER_MEMO" -> "고객 메모";
      case "MEMBERSHIP_POLICY" -> "멤버십 정책";
      case "LOYALTY_POLICY" -> "로열티 정책";
      case "CUSTOMER_TIMELINE" -> "고객 타임라인";
      case "RESERVATION" -> "예약";
      case "RESERVATION_PACKAGE" -> "예약 패키지";
      case "CHECK_IN_OUT" -> "체크인/아웃";
      case "STAYS" -> "숙박";
      case "FACILITY_USAGE" -> "시설 이용";
      case "INQUIRY" -> "문의";
      case "ACCIDENT" -> "사건사고";
      case "MESSAGE" -> "메시지";
      case "PERMISSION" -> "권한";
      default -> resource;
    };
  }

  private String getKoreanAction(String action) {
    return switch (action) {
      case "CREATE" -> "생성";
      case "READ" -> "상세 조회";
      case "LIST" -> "목록 조회";
      case "UPDATE" -> "수정";
      case "DELETE" -> "삭제";
      default -> action;
    };
  }
}
