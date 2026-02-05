package com.gaekdam.gaekdambe.customer_service.membership.command.application.service;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ChangeSource;
import com.gaekdam.gaekdambe.customer_service.membership.command.application.dto.request.MembershipGradeRequest;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.MembershipGradeStatus;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity.Membership;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity.MembershipGrade;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity.MembershipHistory;
import com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository.MembershipGradeRepository;
import com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository.MembershipHistoryRepository;
import com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository.MembershipRepository;
import com.gaekdam.gaekdambe.customer_service.membership.query.mapper.MembershipBatchMapper;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.AuditLog;
import com.gaekdam.gaekdambe.iam_service.log.command.application.service.AuditLogService;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipGradeCommandService {
    private final MembershipBatchMapper membershipBatchMapper;
    private final MembershipRepository membershipRepository;
    private final MembershipHistoryRepository membershipHistoryRepository;
    private final HotelGroupRepository hotelGroupRepository;
    private final MembershipGradeRepository membershipGradeRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditLogService auditLogService;

    // 멤버십 생성
    @Transactional
    @AuditLog(details = "'멤버십 등급 이름: '+ #request.gradeName", type = PermissionTypeKey.MEMBERSHIP_POLICY_CREATE)
    public String createMembershipGrade(MembershipGradeRequest request, Long hotelGroupCode) {
        if (request.gradeName() == null || request.gradeName().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INCORRECT_FORMAT);
        }
        if (request.tierLevel() == null) {
            throw new CustomException(ErrorCode.INVALID_INCORRECT_FORMAT);
        }
        if (request.tierComment() == null || request.tierComment().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INCORRECT_FORMAT);
        }

        HotelGroup hotelGroup = hotelGroupRepository.findById(hotelGroupCode)
                .orElseThrow(() -> new CustomException(ErrorCode.HOTEL_GROUP_NOT_FOUND));

        MembershipGrade membershipGrade = MembershipGrade.registerMembershipGrade(
                hotelGroup,
                request.gradeName(),
                request.tierLevel(),
                request.tierComment(),
                request.calculationAmount(),
                request.calculationCount()

        );
        membershipGradeRepository.save(membershipGrade);
        return "멤버십 등급 생성 완료";
    }

    @Transactional
    @AuditLog(details = "'멤버십 등급 이름: '+ #membershipGradeCode", type = PermissionTypeKey.MEMBERSHIP_POLICY_DELETE)
    public String deleteMembershipGrade(Long hotelGroupCode, Long membershipGradeCode) {
        MembershipGrade membershipGrade = membershipGradeRepository.findById(membershipGradeCode)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBERSHIP_GRADE_NOT_FOUND));

        // 멤버십 등급의 호텔그룹 코드 일치 검사
        if (!membershipGrade.getHotelGroup().getHotelGroupCode().equals(hotelGroupCode)) {
            throw new CustomException(ErrorCode.HOTEL_GROUP_CODE_NOT_MATCH);
        }
        if (membershipGrade.getMembershipGradeStatus() == MembershipGradeStatus.INACTIVE) {
            throw new CustomException(ErrorCode.MEMBERSHIP_GRADE_ALREADY_INACTIVE);
        }
        membershipGrade.deleteMemberShipGradeStatus();

        membershipGradeRepository.save(membershipGrade);
        return "멤버십이 등급이 삭제 되었습니다";

    }

    // @AuditLog 제거: 수동 로깅 (Diff)
    @Transactional
    public String updateMembershipGrade(Long hotelGroupCode, Long membershipGradeCode,
                                        MembershipGradeRequest request, String accessorLoginId) {
        if (request.gradeName() == null || request.gradeName().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INCORRECT_FORMAT);
        }

        if (request.tierLevel() == null) {
            throw new CustomException(ErrorCode.INVALID_INCORRECT_FORMAT);
        }
        if (request.tierComment() == null || request.tierComment().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INCORRECT_FORMAT);
        }

        MembershipGrade membershipGrade = membershipGradeRepository.findById(membershipGradeCode)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBERSHIP_GRADE_NOT_FOUND));

        if (!membershipGrade.getHotelGroup().getHotelGroupCode().equals(hotelGroupCode)) {
            throw new CustomException(ErrorCode.HOTEL_GROUP_CODE_NOT_MATCH);
        }

        Employee accessor = employeeRepository
                .findByLoginId(accessorLoginId).orElseThrow(() -> new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND));

        // --- 변경 전 데이터 캡처 ---
        String prevName = membershipGrade.getGradeName();
        String prevTier = String.valueOf(membershipGrade.getTierLevel());
        String prevAmount = String.valueOf(membershipGrade.getCalculationAmount());
        String prevCount = String.valueOf(membershipGrade.getCalculationCount());

        membershipGrade.update(
                request.gradeName(),
                request.tierLevel(),
                request.tierComment(),
                request.calculationAmount(),
                request.calculationCount());

        // --- 변경 후 데이터 캡처 & 비교 ---
        String newName = membershipGrade.getGradeName();
        String newTier = String.valueOf(membershipGrade.getTierLevel());
        String newAmount = String.valueOf(membershipGrade.getCalculationAmount());
        String newCount = String.valueOf(membershipGrade.getCalculationCount());

        StringBuilder changes = new StringBuilder();
        StringBuilder prevVal = new StringBuilder();
        StringBuilder newVal = new StringBuilder();

        if (!prevName.equals(newName)) {
            changes.append(String.format("[등급명: %s -> %s] ", prevName, newName));
            prevVal.append(String.format("Name: %s, ", prevName));
            newVal.append(String.format("Name: %s, ", newName));
        }
        if (!prevTier.equals(newTier)) {
            changes.append(String.format("[티어: %s -> %s] ", prevTier, newTier));
            prevVal.append(String.format("Tier: %s, ", prevTier));
            newVal.append(String.format("Tier: %s, ", newTier));
        }
        if (!prevAmount.equals(newAmount)) {
            changes.append(String.format("[기준금액: %s -> %s] ", prevAmount, newAmount));
            prevVal.append(String.format("Amt: %s, ", prevAmount));
            newVal.append(String.format("Amt: %s, ", newAmount));
        }
        if (!prevCount.equals(newCount)) {
            changes.append(String.format("[기준횟수: %s -> %s] ", prevCount, newCount));
            prevVal.append(String.format("Cnt: %s, ", prevCount));
            newVal.append(String.format("Cnt: %s, ", newCount));
        }

        String details = changes.length() > 0 ? changes.toString() : "멤버십 등급 수정";

        auditLogService.saveAuditLog(
                accessor,
                PermissionTypeKey.MEMBERSHIP_POLICY_UPDATE,
                details,
                (prevVal.length() > 0 ? prevVal.toString() : null),
                (newVal.length() > 0 ? newVal.toString() : null));

        return "등급 정보가 수정 되었습니다";
    }

    @Scheduled(cron = "0 10 19 * * *", zone = "Asia/Seoul") // 매년 1월 1일 06:38 실행
    @Transactional
    public void updateMembershipGrades() {
        log.info("Membership Grade Info Update Batch Start (Annual)");
        LocalDate today = LocalDate.now();

        // 1월 1일이 아니라면 실행하지 않음 (이중 방어)
/*        if (today.getMonthValue() != 1 || today.getDayOfMonth() != 1) {
            return;
        }*/

        // 모든 호텔 그룹 조회 (조건 없는 일괄 갱신)
        List<HotelGroup> allGroups = hotelGroupRepository.findAll();

        for (HotelGroup group : allGroups) {
            processHotelGroup(group.getHotelGroupCode(), today);
        }

        log.info("Membership Grade Info Update Batch End");
    }

    private void processHotelGroup(Long hotelGroupCode, LocalDate today) {
        // 해당 호텔 그룹의 모든 등급 정책 조회 (Tier 높은 순 정렬)
        List<MembershipGrade> allGrades = membershipGradeRepository.findAllByHotelGroup_HotelGroupCode(
                hotelGroupCode);
        allGrades.sort(Comparator.comparing(MembershipGrade::getTierLevel).reversed()); // 높은 티어부터 검사

        if (allGrades.isEmpty()) {
            return;
        }

        // 조건: 작년 1월 1일 ~ 작년 12월 31일 실적 기준
        LocalDate startDate = today.minusYears(1); // 작년 1월 1일
        LocalDate endDate = today.minusDays(1); // 작년 12월 31일

        // 해당 호텔 그룹의 모든 멤버십 조회
        List<Membership> memberships = membershipRepository.findAllByHotelGroupCode(hotelGroupCode);

        for (Membership membership : memberships) {
            updateMemberGrade(membership, allGrades, startDate, endDate, today);
        }
    }


    private void updateMemberGrade(Membership membership, List<MembershipGrade> allGrades,
                                   LocalDate startDate, LocalDate endDate, LocalDate today) {
        // 실적 조회
        Map<String, Object> stats = membershipBatchMapper.selectCustomerStatistics(
                membership.getCustomerCode(),
                startDate, endDate);
        BigDecimal totalAmount = (BigDecimal) stats.get("totalAmount"); // null safe in query (IFNULL)
        Long visitCount = (Long) stats.get("visitCount");

        // 적절한 등급 찾기
        MembershipGrade newGrade = findBestGrade(allGrades, totalAmount, visitCount);

        // 등급 변경이 필요한 경우
        if (newGrade != null && !newGrade.getMembershipGradeCode()
                .equals(membership.getMembershipGradeCode())) {
            MembershipGrade currentGrade = allGrades.stream()
                    .filter(g -> g.getMembershipGradeCode().equals(membership.getMembershipGradeCode()))
                    .findFirst()
                    .orElse(null);

            String beforeGradeName = (currentGrade != null) ? currentGrade.getGradeName() : "Unknown";

            // 멤버십 업데이트
            membership.changeMembership(
                    newGrade.getMembershipGradeCode(),
                    membership.getMembershipStatus(), // 상태 유지
                    membership.getExpiredAt(), // 만료일 유지 (등급 변경 시 만료일 정책이 있다면 수정 필요)
                    LocalDateTime.now());

            membershipRepository.saveAndFlush(membership);
            // 이력 저장
            MembershipHistory history = MembershipHistory.recordMembershipChange(
                    membership.getCustomerCode(),
                    membership.getMembershipCode(),
                    ChangeSource.SYSTEM,
                    null, // System changed
                    "Automatic Grade Update based on performance",
                    beforeGradeName,
                    newGrade.getGradeName(),
                    membership.getMembershipStatus(),
                    membership.getMembershipStatus(),
                    membership.getExpiredAt(), // 이전 만료일
                    membership.getExpiredAt(), // 이후 만료일
                    LocalDateTime.now(),
                    newGrade.getMembershipGradeCode());
            membershipHistoryRepository.save(history);

            log.info("Updated membership grade for customer: {}, {} -> {}",
                    membership.getCustomerCode(), beforeGradeName, newGrade.getGradeName());
        }
    }

    private MembershipGrade findBestGrade(List<MembershipGrade> allGrades, BigDecimal amount,
                                          Long count) {
        // 높은 티어부터 순회하며 조건 만족 시 반환
        for (MembershipGrade grade : allGrades) {
            boolean amountMet = (grade.getCalculationAmount() == null)
                    || (amount.compareTo(BigDecimal.valueOf(grade.getCalculationAmount())) >= 0);
            boolean countMet = (grade.getCalculationCount() == null) || (count >= grade.getCalculationCount());

            if (amountMet && countMet) {
                return grade;
            }
        }
        // 실적 미달 시 최하위로 강등 -> allGrades.get(allGrades.size() - 1)
        return !allGrades.isEmpty() ? allGrades.get(allGrades.size() - 1) : null;
    }
}
