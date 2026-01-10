package com.gaekdam.gaekdambe.dummy.generate.customer_service.loyalty;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ChangeSource;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.Customer;
import com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository.CustomerRepository;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.LoyaltyGradeName;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.LoyaltyStatus;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.Loyalty;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.LoyaltyGrade;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.LoyaltyHistory;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.infrastructure.repository.LoyaltyGradeRepository;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.infrastructure.repository.LoyaltyHistoryRepository;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.infrastructure.repository.LoyaltyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Component
public class DummyLoyaltyDataTest {

    @Autowired private CustomerRepository customerRepository;

    @Autowired private LoyaltyGradeRepository loyaltyGradeRepository;
    @Autowired private LoyaltyRepository loyaltyRepository;
    @Autowired private LoyaltyHistoryRepository loyaltyHistoryRepository;

    private final Map<Long, List<LoyaltyGrade>> gradeCache = new HashMap<>();

    private static final double LOYALTY_RATE = 0.80;
    private static final double LOYALTY_CHANGE_RATE = 0.20;

    @Transactional
    public void generate() {

        List<Customer> customers = customerRepository.findAll();
        if (customers.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();

        List<Long> hotelGroupCodes = customers.stream()
                .map(Customer::getHotelGroupCode)
                .distinct()
                .collect(Collectors.toList());

        seedLoyaltyGradesIfNeeded(hotelGroupCodes, now);

        for (Customer customer : customers) {
            createLoyaltyIfNeeded(customer, now);
        }
    }

    /**
     * loyalty_grade 테이블: GENERAL/EXCELLENT 전역 2개만 유지(없는 것만 insert)
     * hotelGroupCodes별로는 캐시만 공유
     */
    private void seedLoyaltyGradesIfNeeded(List<Long> hotelGroupCodes, LocalDateTime now) {

        if (hotelGroupCodes == null || hotelGroupCodes.isEmpty()) {
            throw new IllegalArgumentException("hotelGroupCodes is empty");
        }

        List<LoyaltyGrade> all = loyaltyGradeRepository.findAll();

        Map<LoyaltyGradeName, LoyaltyGrade> unique = new EnumMap<>(LoyaltyGradeName.class);
        for (LoyaltyGrade g : all) {
            unique.putIfAbsent(g.getGradeName(), g);
        }

        Long seedHotelGroupCode = hotelGroupCodes.get(0);
        String standard = "{\"windowMonths\":12}";

        unique.putIfAbsent(
                LoyaltyGradeName.GENERAL,
                loyaltyGradeRepository.save(
                        LoyaltyGrade.registerLoyaltyGrade(seedHotelGroupCode, LoyaltyGradeName.GENERAL, 1L, standard, true, now)
                )
        );
        unique.putIfAbsent(
                LoyaltyGradeName.EXCELLENT,
                loyaltyGradeRepository.save(
                        LoyaltyGrade.registerLoyaltyGrade(seedHotelGroupCode, LoyaltyGradeName.EXCELLENT, 2L, standard, true, now)
                )
        );

        List<LoyaltyGrade> grades = new ArrayList<>(unique.values());
        grades.sort(Comparator.comparing(LoyaltyGrade::getTierLevel));

        for (Long hg : hotelGroupCodes) {
            gradeCache.put(hg, grades);
        }
    }

    private void createLoyaltyIfNeeded(Customer customer, LocalDateTime now) {
        if (!chance(LOYALTY_RATE)) return;

        Long customerCode = customer.getCustomerCode();
        Long hg = customer.getHotelGroupCode();

        Optional<Loyalty> existing = loyaltyRepository.findByHotelGroupCodeAndCustomerCode(hg, customerCode);
        if (existing.isPresent()) return;

        List<LoyaltyGrade> grades = gradeCache.get(hg);
        if (grades == null || grades.isEmpty()) {
            throw new IllegalStateException("LoyaltyGrade not seeded for hotelGroupCode=" + hg);
        }

        LoyaltyGrade picked = pickLoyaltyGrade(grades);
        LocalDateTime joinedAt = now.minusDays(randomInt(0, 30));

        Loyalty loyalty = Loyalty.registerLoyalty(
                customerCode,
                hg,
                picked.getLoyaltyGradeCode(),
                joinedAt,
                now
        );
        loyaltyRepository.save(loyalty);

        // 등급 부여 이력 1건
        loyaltyHistoryRepository.save(
                LoyaltyHistory.recordLoyaltyChange(
                        customerCode,
                        loyalty.getLoyaltyCode(),
                        ChangeSource.SYSTEM,
                        null,
                        "더미 등급 부여",
                        null,
                        picked.getLoyaltyGradeCode(),
                        null,
                        LoyaltyStatus.ACTIVE,
                        joinedAt
                )
        );

        // 일부 등급 변경 이력
        if (chance(LOYALTY_CHANGE_RATE)) {
            LoyaltyGrade after = pickOtherGrade(grades, picked);
            LocalDateTime changedAt = now.plusDays(randomInt(1, 30));

            loyalty.changeLoyaltyGrade(after.getLoyaltyGradeCode(), changedAt);

            loyaltyHistoryRepository.save(
                    LoyaltyHistory.recordLoyaltyChange(
                            customerCode,
                            loyalty.getLoyaltyCode(),
                            ChangeSource.MANUAL,
                            1L,
                            "더미 등급 변경",
                            picked.getLoyaltyGradeCode(),
                            after.getLoyaltyGradeCode(),
                            LoyaltyStatus.ACTIVE,
                            LoyaltyStatus.ACTIVE,
                            changedAt
                    )
            );
        }
    }

    private LoyaltyGrade pickLoyaltyGrade(List<LoyaltyGrade> grades) {
        double r = ThreadLocalRandom.current().nextDouble();
        if (r < 0.80) return findGrade(grades, LoyaltyGradeName.GENERAL);
        return findGrade(grades, LoyaltyGradeName.EXCELLENT);
    }

    private LoyaltyGrade pickOtherGrade(List<LoyaltyGrade> grades, LoyaltyGrade current) {
        LoyaltyGradeName currentName = current.getGradeName();
        LoyaltyGradeName target =
                (currentName == LoyaltyGradeName.GENERAL) ? LoyaltyGradeName.EXCELLENT : LoyaltyGradeName.GENERAL;
        return findGrade(grades, target);
    }

    private LoyaltyGrade findGrade(List<LoyaltyGrade> grades, LoyaltyGradeName name) {
        return grades.stream()
                .filter(g -> g.getGradeName() == name)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("LoyaltyGrade not found: " + name));
    }

    private boolean chance(double probability) {
        return ThreadLocalRandom.current().nextDouble() < probability;
    }

    private int randomInt(int minInclusive, int maxExclusive) {
        return ThreadLocalRandom.current().nextInt(minInclusive, maxExclusive);
    }
}
