package com.gaekdam.gaekdambe.dummy.generate.customer_service.loyalty;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ChangeSource;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.Customer;
import com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository.CustomerRepository;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.LoyaltyStatus;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.Loyalty;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.LoyaltyGrade;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.LoyaltyHistory;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.infrastructure.repository.LoyaltyGradeRepository;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.infrastructure.repository.LoyaltyHistoryRepository;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.infrastructure.repository.LoyaltyRepository;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DummyLoyaltyDataTest {

    @Autowired private CustomerRepository customerRepository;

    @Autowired private LoyaltyGradeRepository loyaltyGradeRepository;
    @Autowired private LoyaltyRepository loyaltyRepository;
    @Autowired private LoyaltyHistoryRepository loyaltyHistoryRepository;

    @Autowired private HotelGroupRepository hotelGroupRepository; // 변경: 호텔 테이블에서 코드 읽기

    private final Map<Long, List<LoyaltyGrade>> gradeCache = new HashMap<>();

    private static final double LOYALTY_RATE = 0.80;
    private static final double LOYALTY_CHANGE_RATE = 0.20;

    private static final String STANDARD = "{\"windowMonths\":12}";
    private static final List<GradeSeed> GRADE_SEEDS = List.of(
            new GradeSeed("GENERAL", 1L,"일반"),
            new GradeSeed("EXCELLENT", 2L,"우수")

    );

    @Transactional
    public void generate() {

        List<Customer> customers = customerRepository.findAll();
        if (customers.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();

        List<Long> hotelGroupCodes = hotelGroupRepository.findAll().stream()
                .map(HotelGroup::getHotelGroupCode)
                .toList();

        if (hotelGroupCodes.isEmpty()) return;

        seedLoyaltyGradesIfNeeded(hotelGroupCodes, now);

        for (Customer customer : customers) {
            createLoyaltyIfNeeded(customer, now);
        }
    }

    private void seedLoyaltyGradesIfNeeded(List<Long> hotelGroupCodes, LocalDateTime now) {

        List<LoyaltyGrade> all = loyaltyGradeRepository.findAll();

        Map<Long, Map<String, LoyaltyGrade>> byHg = new HashMap<>();
        for (LoyaltyGrade g : all) {
            byHg.computeIfAbsent(g.getHotelGroup().getHotelGroupCode(), k -> new HashMap<>())
                    .putIfAbsent(g.getLoyaltyGradeName(), g);
        }

        for (Long hg : hotelGroupCodes) {
            Map<String, LoyaltyGrade> existing = byHg.computeIfAbsent(hg, k -> new HashMap<>());

            for (GradeSeed seed : GRADE_SEEDS) {
                if (!existing.containsKey(seed.gradeName())) {
                  HotelGroup hotelGroup =hotelGroupRepository.findById(hg).orElseThrow();
                    LoyaltyGrade saved = loyaltyGradeRepository.save(
                            LoyaltyGrade.registerLoyaltyGrade(
                                hotelGroup,
                                seed.gradeName(),
                                seed.tierLevel(),
                                seed.tierComment(),
                                1000000L,
                                3,
                                12,
                                1
                            )
                    );
                    existing.put(saved.getLoyaltyGradeName(), saved);
                }
            }

            List<LoyaltyGrade> grades = new ArrayList<>(existing.values());
            grades.sort(Comparator.comparing(LoyaltyGrade::getLoyaltyTierLevel));
            gradeCache.put(hg, grades);
        }
    }

    private void createLoyaltyIfNeeded(Customer customer, LocalDateTime now) {
        if (!chance(LOYALTY_RATE)) return;

        Long customerCode = customer.getCustomerCode();
        Long hg = customer.getHotelGroupCode();

        if (loyaltyRepository.findByHotelGroupCodeAndCustomerCode(hg, customerCode).isPresent()) return;

        List<LoyaltyGrade> grades = gradeCache.get(hg);
        if (grades == null || grades.isEmpty()) return;

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

        // 상태 다양화(상세 최신 1건 정책 테스트용)
        if (chance(0.10)) loyalty.changeLoyaltyStatus(LoyaltyStatus.INACTIVE, now.plusDays(5));
    }

    private LoyaltyGrade pickLoyaltyGrade(List<LoyaltyGrade> grades) {
        double r = ThreadLocalRandom.current().nextDouble();
        if (r < 0.80) return findGrade(grades, "GENERAL");
        return findGrade(grades, "EXCELLENT");
    }

    private LoyaltyGrade pickOtherGrade(List<LoyaltyGrade> grades, LoyaltyGrade current) {
        String target = "GENERAL".equals(current.getLoyaltyGradeName()) ? "EXCELLENT" : "GENERAL";
        return findGrade(grades, target);
    }

    private LoyaltyGrade findGrade(List<LoyaltyGrade> grades, String name) {
        return grades.stream()
                .filter(g -> name.equals(g.getLoyaltyGradeName()))
                .findFirst()
                .orElseGet(() -> grades.get(ThreadLocalRandom.current().nextInt(grades.size())));
    }

    private boolean chance(double probability) {
        return ThreadLocalRandom.current().nextDouble() < probability;
    }

    private int randomInt(int minInclusive, int maxExclusive) {
        return ThreadLocalRandom.current().nextInt(minInclusive, maxExclusive);
    }

    private record GradeSeed(String gradeName, Long tierLevel,String tierComment) {}
}
