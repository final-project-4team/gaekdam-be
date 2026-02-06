package com.gaekdam.gaekdambe.customer_service.loyalty.command.appliaction.service;

import com.gaekdam.gaekdambe.customer_service.loyalty.command.appliaction.dto.request.LoyaltyGradeRequest;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.LoyaltyGradeStatus;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.LoyaltyGrade;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.infrastructure.repository.LoyaltyGradeRepository;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.AuditLog;
import com.gaekdam.gaekdambe.iam_service.log.command.application.service.AuditLogService;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoyaltyGradeCommandService {

  private final HotelGroupRepository hotelGroupRepository;
  private final LoyaltyGradeRepository loyaltyGradeRepository;
  private final EmployeeRepository employeeRepository;
  private final AuditLogService auditLogService;

  @Transactional
  @AuditLog(details = "'로열티 등급 이름 : '+ #request.loyaltyGradeName", type = PermissionTypeKey.LOYALTY_POLICY_CREATE)
  public String createLoyaltyGrade(LoyaltyGradeRequest request, Long hotelGroupCode) {

    HotelGroup hotelGroup = hotelGroupRepository.findById(hotelGroupCode)
        .orElseThrow(() -> new CustomException(ErrorCode.HOTEL_GROUP_NOT_FOUND));

    LoyaltyGrade loyaltyGrade = LoyaltyGrade.registerLoyaltyGrade(
        hotelGroup,
        request.loyaltyGradeName(),
        request.loyaltyTierLevel(),
        request.loyaltyTierComment(),
        request.loyaltyCalculationAmount(),
        request.loyaltyCalculationCount()
    );
    loyaltyGradeRepository.save(loyaltyGrade);
    return "멤버십 등급 생성 완료";
  }

  @Transactional
  @AuditLog(details = "'로열티 등급 코드 : '+ #loyaltyGradeCode", type = PermissionTypeKey.LOYALTY_POLICY_DELETE)
  public String deleteLoyaltyGrade(Long hotelGroupCode, Long loyaltyGradeCode) {
    LoyaltyGrade loyaltyGrade = loyaltyGradeRepository.findById(loyaltyGradeCode)
        .orElseThrow(() -> new CustomException(ErrorCode.LOYALTY_GRADE_NOT_FOUND));

    if (!loyaltyGrade.getHotelGroup().getHotelGroupCode().equals(hotelGroupCode)) {
      throw new CustomException(ErrorCode.HOTEL_GROUP_CODE_NOT_MATCH);
    }
    if (loyaltyGrade.getLoyaltyGradeStatus() == LoyaltyGradeStatus.INACTIVE) {
      throw new CustomException(ErrorCode.LOYALTY_GRADE_ALREADY_INACTIVE);
    }
    loyaltyGrade.deleteLoyaltyGradeStatus();

    loyaltyGradeRepository.save(loyaltyGrade);
    return "멤버십이 등급이 삭제 되었습니다";

  }

  @Transactional
  public String updateLoyaltyGrade(Long hotelGroupCode, Long loyaltyGradeCode,
      LoyaltyGradeRequest request, String accessorLoginId) {

    LoyaltyGrade loyaltyGrade = loyaltyGradeRepository.findById(loyaltyGradeCode)
        .orElseThrow(() -> new CustomException(ErrorCode.LOYALTY_GRADE_NOT_FOUND));

    if (!loyaltyGrade.getHotelGroup().getHotelGroupCode().equals(hotelGroupCode)) {
      throw new CustomException(ErrorCode.HOTEL_GROUP_CODE_NOT_MATCH);
    }
    String prevName = loyaltyGrade.getLoyaltyGradeName();
    Long prevInfoAmount = loyaltyGrade.getLoyaltyCalculationAmount();
    Integer prevInfoCount = loyaltyGrade.getLoyaltyCalculationCount();
    Integer prevInfoTerm = loyaltyGrade.getLoyaltyCalculationTermMonth();
    Integer prevInfoRenewal = loyaltyGrade.getLoyaltyCalculationRenewalDay();

    loyaltyGrade.update(
        request.loyaltyGradeName(),
        request.loyaltyTierLevel(),
        request.loyaltyTierComment(),
        request.loyaltyCalculationAmount(),
        request.loyaltyCalculationCount());

    StringBuilder changes = new StringBuilder();

    // 등급 이름 비교
    String newName = loyaltyGrade.getLoyaltyGradeName();
    if (prevName != null && !prevName.equals(newName)) {
      changes.append(String.format("[등급명: %s -> %s] ", prevName, newName));
    }
    // 기준 금액 비교
    Long newInfoAmount = loyaltyGrade.getLoyaltyCalculationAmount();
    if (prevInfoAmount == null) {
      if (newInfoAmount != null) {
        changes.append(String.format("[기준금액: 없음 -> %d] ", newInfoAmount));
      }
    } else if (!prevInfoAmount.equals(newInfoAmount)) {
      // newInfoAmount가 null인 경우 처리 포함
      changes.append(String.format("[기준금액: %d -> %s] ", prevInfoAmount,
          (newInfoAmount != null ? newInfoAmount.toString() : "없음")));
    }
    // 기준 횟수 비교
    Integer newInfoCount = loyaltyGrade.getLoyaltyCalculationCount();
    if (prevInfoCount == null) {
      if (newInfoCount != null) {
        changes.append(String.format("[기준횟수: 없음 -> %d] ", newInfoCount));
      }
    } else if (!prevInfoCount.equals(newInfoCount)) {
      changes.append(String.format("[기준횟수: %d -> %s] ", prevInfoCount,
          (newInfoCount != null ? newInfoCount.toString() : "없음")));
    }
    // 기준 기간(월) 비교
    Integer newInfoTerm = loyaltyGrade.getLoyaltyCalculationTermMonth();
    if (prevInfoTerm == null) {
      if (newInfoTerm != null)
        changes.append(String.format("[기간: 없음 -> %d] ", newInfoTerm));
    } else if (!prevInfoTerm.equals(newInfoTerm)) {
      changes.append(String.format("[기간: %d -> %s] ", prevInfoTerm, (newInfoTerm != null ? newInfoTerm : "없음")));
    }
    // 갱신일 비교
    Integer newInfoRenewal = loyaltyGrade.getLoyaltyCalculationRenewalDay();
    if (prevInfoRenewal == null) {
      if (newInfoRenewal != null)
        changes.append(String.format("[갱신일: 없음 -> %d] ", newInfoRenewal));
    } else if (!prevInfoRenewal.equals(newInfoRenewal)) {
      changes
          .append(String.format("[갱신일: %d -> %s] ", prevInfoRenewal, (newInfoRenewal != null ? newInfoRenewal : "없음")));
    }

    Employee accessor = employeeRepository.findByLoginId(accessorLoginId).orElse(null);
    if (accessor != null) {
      auditLogService.saveAuditLog(
          accessor,
          PermissionTypeKey.LOYALTY_POLICY_UPDATE,
          changes.toString(), // "이름: A->B, 금액: 100->200"
          null,
          null);
    }
    return "등급 정보가 수정 되었습니다";
  }
}
