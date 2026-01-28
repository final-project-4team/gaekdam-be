package com.gaekdam.gaekdambe.customer_service.loyalty.command.appliaction.service;

import com.gaekdam.gaekdambe.customer_service.loyalty.command.appliaction.dto.request.LoyaltyGradeRequest;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.LoyaltyGradeStatus;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.LoyaltyGrade;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.infrastructure.repository.LoyaltyGradeRepository;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.AuditLog;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoyaltyGradeCommandService {
  private final HotelGroupRepository hotelGroupRepository;
  private final LoyaltyGradeRepository loyaltyGradeRepository;


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
        request.loyaltyCalculationCount(),
        request.loyaltyCalculationTermMonth(),
        request.loyaltyCalculationRenewalDay()

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
  public String updateLoyaltyGrade(Long hotelGroupCode, Long loyaltyGradeCode, LoyaltyGradeRequest request) {

    LoyaltyGrade loyaltyGrade = loyaltyGradeRepository.findById(loyaltyGradeCode)
        .orElseThrow(() -> new CustomException(ErrorCode.LOYALTY_GRADE_NOT_FOUND));

    if (!loyaltyGrade.getHotelGroup().getHotelGroupCode().equals(hotelGroupCode)) {
      throw new CustomException(ErrorCode.HOTEL_GROUP_CODE_NOT_MATCH);
    }

    loyaltyGrade.update(
        request.loyaltyGradeName(),
        request.loyaltyTierLevel(),
        request.loyaltyTierComment(),
        request.loyaltyCalculationAmount(),
        request.loyaltyCalculationCount(),
        request.loyaltyCalculationTermMonth(),
        request.loyaltyCalculationRenewalDay());

    return "등급 정보가 수정 되었습니다";
  }
}
