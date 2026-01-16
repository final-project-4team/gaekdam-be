package com.gaekdam.gaekdambe.customer_service.membership.command.application.service;

import com.gaekdam.gaekdambe.customer_service.membership.command.application.dto.request.MembershipGradeRequest;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.MembershipGradeStatus;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity.MembershipGrade;
import com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository.MembershipGradeRepository;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MembershipGradeCommandService {
  private final HotelGroupRepository hotelGroupRepository;
  private final MembershipGradeRepository membershipGradeRepository;

  // 멤버십 생성
  @Transactional
  public String createMembershipGrade(MembershipGradeRequest request, Long hotelGroupCode) {
    if (request.gradeName() == null || request.gradeName().isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_INCORRECT_FORMAT);
    }
    if (request.tierLevel() == null) {
      throw new CustomException(ErrorCode.INVALID_INCORRECT_FORMAT);
    }
    if (request.tierComment() == null || request.tierComment().isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_INCORRECT_FORMAT);
    }
    if (request.calculationTermMonth() == null) {
      throw new CustomException(ErrorCode.INVALID_INCORRECT_FORMAT);
    }
    if (request.calculationRenewalDay() == null) {
      throw new CustomException(ErrorCode.INVALID_INCORRECT_FORMAT);
    }

    HotelGroup hotelGroup = hotelGroupRepository.findById(hotelGroupCode)
        .orElseThrow(() -> new IllegalArgumentException("Hotel code not found"));

    MembershipGrade membershipGrade = MembershipGrade.registerMembershipGrade(
        hotelGroup,
        request.gradeName(),
        request.tierLevel(),
        request.tierComment(),
        request.calculationAmount(),
        request.calculationCount(),
        request.calculationTermMonth(),
        request.calculationRenewalDay()

    );
    membershipGradeRepository.save(membershipGrade);
    return "멤버십 등급 생성 완료";
  }

  @Transactional
  public String deleteMembershipGrade(Long hotelGroupCode, Long membershipGradeCode) {
    MembershipGrade membershipGrade = membershipGradeRepository.findById(membershipGradeCode)
        .orElseThrow(() -> new IllegalArgumentException("Hotel Group not found"));

    // 멤버십 등급의 호텔그룹 코드 일치 검사
    if (!membershipGrade.getHotelGroup().getHotelGroupCode().equals(hotelGroupCode)) {
      throw new CustomException(ErrorCode.HOTEL_GROUP_CODE_NOT_MATCH);
    }
    if (membershipGrade.getMembershipGradeStatus() == MembershipGradeStatus.INACTIVE) {
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    }
    membershipGrade.deleteMemberShipGradeStatus();

    membershipGradeRepository.save(membershipGrade);
    return "멤버십이 등급이 삭제 되었습니다";

  }

  @Transactional
  public String updateMembershipGrade(Long hotelGroupCode, Long membershipGradeCode, MembershipGradeRequest request) {
    if (request.gradeName() == null || request.gradeName().isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_INCORRECT_FORMAT);
    }
    if (request.tierLevel() == null) {
      throw new CustomException(ErrorCode.INVALID_INCORRECT_FORMAT);
    }
    if (request.tierComment() == null || request.tierComment().isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_INCORRECT_FORMAT);
    }
    if (request.calculationTermMonth() == null) {
      throw new CustomException(ErrorCode.INVALID_INCORRECT_FORMAT);
    }
    if (request.calculationRenewalDay() == null) {
      throw new CustomException(ErrorCode.INVALID_INCORRECT_FORMAT);
    }

    MembershipGrade membershipGrade = membershipGradeRepository.findById(membershipGradeCode)
        .orElseThrow(() -> new IllegalArgumentException("Membership Grade not found"));

    if (!membershipGrade.getHotelGroup().getHotelGroupCode().equals(hotelGroupCode)) {
      throw new CustomException(ErrorCode.HOTEL_GROUP_CODE_NOT_MATCH);
    }

    membershipGrade.update(
        request.gradeName(),
        request.tierLevel(),
        request.tierComment(),
        request.calculationAmount(),
        request.calculationCount(),
        request.calculationTermMonth(),
        request.calculationRenewalDay());

    return "등급 정보가 수정 되었습니다";
  }
}
