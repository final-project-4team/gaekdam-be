package com.gaekdam.gaekdambe.customer_service.loyalty.command.appliaction.service;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ChangeSource;

import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.Loyalty;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.LoyaltyGrade;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.LoyaltyHistory;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.infrastructure.repository.LoyaltyGradeRepository;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.infrastructure.repository.LoyaltyHistoryRepository;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.infrastructure.repository.LoyaltyRepository;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoyaltyGradeCalculationService {

    private final LoyaltyRepository loyaltyRepository;
    private final LoyaltyHistoryRepository loyaltyHistoryRepository;
    private final LoyaltyGradeRepository loyaltyGradeRepository;
    private final HotelGroupRepository hotelGroupRepository;
    private final com.gaekdam.gaekdambe.customer_service.loyalty.query.service.LoyaltyGradeQueryService loyaltyGradeQueryService;

    @Scheduled(cron = "0 5 18 * * *", zone = "Asia/Seoul") // 매년 1월 1일 07:38 실행 (멤버십 배치 후)
    @Transactional
    public void updateLoyaltyGrades() {
        log.info("Loyalty Grade Info Update Batch Start (Annual)");
        LocalDate today = LocalDate.now();

        if (today.getMonthValue() != 1 || today.getDayOfMonth() != 1) {
            return;
        }

        List<HotelGroup> allGroups = hotelGroupRepository.findAll();

        for (HotelGroup group : allGroups) {
            processHotelGroup(group.getHotelGroupCode(), today);
        }
        log.info("Loyalty Grade Info Update Batch End");
    }

    private void processHotelGroup(Long hotelGroupCode, LocalDate today) {
        List<LoyaltyGrade> allGrades = loyaltyGradeRepository.findAllByHotelGroup_HotelGroupCode(hotelGroupCode);
        allGrades.sort(Comparator.comparing(LoyaltyGrade::getLoyaltyTierLevel).reversed());

        if (allGrades.isEmpty())
            return;

        // 조건: 작년 1월 1일 ~ 작년 12월 31일 실적 기준
        LocalDate startDate = today.minusYears(1);
        LocalDate endDate = today.minusDays(1);

        List<Loyalty> loyalties = loyaltyRepository.findAllByHotelGroupCode(hotelGroupCode);

        for (Loyalty loyalty : loyalties) {
            updateLoyaltyMemberGrade(loyalty, allGrades, startDate, endDate, today);
        }
    }

    private void updateLoyaltyMemberGrade(Loyalty loyalty, List<LoyaltyGrade> allGrades,
                                          LocalDate startDate, LocalDate endDate, LocalDate today) {

        // Query Service를 통해 다음 등급 계산
        LoyaltyGrade newGrade = loyaltyGradeQueryService.calculateNextLoyaltyGrade(
                loyalty.getCustomerCode(),
                loyalty.getHotelGroupCode());

        if (newGrade != null && !newGrade.getLoyaltyGradeCode().equals(loyalty.getLoyaltyGradeCode())) {

            LoyaltyGrade currentGrade = allGrades.stream()
                    .filter(g -> g.getLoyaltyGradeCode().equals(loyalty.getLoyaltyGradeCode()))
                    .findFirst()
                    .orElse(null);

            String beforeGradeName = (currentGrade != null) ? currentGrade.getLoyaltyGradeName() : "Unknown";

            // 등급 변경
            loyalty.changeLoyaltyGrade(newGrade.getLoyaltyGradeCode(), LocalDateTime.now());

            // 이력 저장
            LoyaltyHistory history = LoyaltyHistory.recordLoyaltyChange(
                    loyalty.getCustomerCode(),
                    loyalty.getLoyaltyCode(),
                    ChangeSource.SYSTEM,
                    null,
                    "Automatic Loyalty Grade Update (Annual)",
                    (currentGrade != null ? currentGrade.getLoyaltyGradeCode() : null), // before code
                    newGrade.getLoyaltyGradeCode(), // after code
                    loyalty.getLoyaltyStatus(), // status changes? Assuming keeping active for now
                    loyalty.getLoyaltyStatus(),
                    LocalDateTime.now());

            loyaltyHistoryRepository.save(history);

            log.info("Updated loyalty grade for customer: {}, {} -> {}",
                    loyalty.getCustomerCode(), beforeGradeName, newGrade.getLoyaltyGradeName());
        }
    }
}
