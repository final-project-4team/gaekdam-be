package com.gaekdam.gaekdambe.customer_service.membership.command.application.service;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ChangeSource;
import com.gaekdam.gaekdambe.customer_service.membership.command.application.dto.request.MembershipManualChangeRequest;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.MembershipGradeStatus;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.MembershipStatus;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity.Membership;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity.MembershipGrade;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity.MembershipHistory;
import com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository.MembershipGradeRepository;
import com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository.MembershipHistoryRepository;
import com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository.MembershipRepository;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MembershipManualCommandService {

    private final MembershipRepository membershipRepository;
    private final MembershipGradeRepository membershipGradeRepository;
    private final MembershipHistoryRepository membershipHistoryRepository;

    @Transactional
    public String changeMembershipManually(
            Long hotelGroupCode,
            Long employeeCode,
            Long customerCode,
            MembershipManualChangeRequest request
    ) {
        // 1) 입력 검증
        if (request == null
                || request.membershipGradeCode() == null
                || request.membershipStatus() == null) {
            throw new CustomException(ErrorCode.INVALID_INCORRECT_FORMAT);
        }

        // employeeCode 필수
        if (employeeCode == null) {
            throw new CustomException(ErrorCode.EMPLOYEE_CODE_REQUIRED);
        }

        // 수동 변경 사유 필수
        if (request.changeReason() == null || request.changeReason().trim().isEmpty()) {
            throw new CustomException(ErrorCode.MEMBERSHIP_MANUAL_REASON_REQUIRED);
        }

        // 2) 변경할 등급 조회 + 호텔그룹 검증 + ACTIVE 검증
        MembershipGrade afterGrade = membershipGradeRepository.findById(request.membershipGradeCode())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBERSHIP_GRADE_NOT_FOUND));

        if (!afterGrade.getHotelGroup().getHotelGroupCode().equals(hotelGroupCode)) {
            throw new CustomException(ErrorCode.HOTEL_GROUP_CODE_NOT_MATCH);
        }
        if (afterGrade.getMembershipGradeStatus() != MembershipGradeStatus.ACTIVE) {
            throw new CustomException(ErrorCode.MEMBERSHIP_GRADE_INACTIVE);
        }

        LocalDateTime now = LocalDateTime.now();

        // 3) 기존 membership 조회
        Membership membership = membershipRepository
                .findByCustomerCodeAndHotelGroupCode(customerCode, hotelGroupCode)
                .orElseGet(() -> membershipRepository.save(
                        Membership.registerMembership(
                                customerCode,
                                hotelGroupCode,
                                afterGrade.getMembershipGradeCode(),
                                now,
                                now
                        )
                ));

        // 4) before 값 (history)
        Long beforeGradeCode = membership.getMembershipGradeCode();
        MembershipStatus beforeStatus = membership.getMembershipStatus();
        LocalDateTime beforeExpiresAt = membership.getExpiredAt();

        String beforeGradeName = null;
        if (beforeGradeCode != null) {
            beforeGradeName = membershipGradeRepository.findById(beforeGradeCode)
                    .map(MembershipGrade::getGradeName)
                    .orElse(null);
        }

        String afterGradeName = afterGrade.getGradeName();
        if (afterGradeName == null || afterGradeName.trim().isEmpty()) {
            throw new CustomException(ErrorCode.MEMBERSHIP_GRADE_NAME_EMPTY);
        }

        MembershipStatus afterStatus = request.membershipStatus();
        LocalDateTime afterExpiresAt = request.expiredAt(); // null 허용

        // 5) membership 변경
        membership.changeMembership(
                afterGrade.getMembershipGradeCode(),
                afterStatus,
                afterExpiresAt,
                now
        );
        membershipRepository.save(membership);

        // 6) history 저장 (MANUAL)
        MembershipHistory history = MembershipHistory.recordMembershipChange(
                customerCode,
                membership.getMembershipCode(),
                ChangeSource.MANUAL,
                employeeCode,
                request.changeReason(),
                beforeGradeName,
                afterGradeName,
                beforeStatus,
                afterStatus,
                beforeExpiresAt,
                afterExpiresAt,
                now,
                afterGrade.getMembershipGradeCode()
        );
        membershipHistoryRepository.save(history);

        return "멤버십 수동 변경 완료";
    }
}
