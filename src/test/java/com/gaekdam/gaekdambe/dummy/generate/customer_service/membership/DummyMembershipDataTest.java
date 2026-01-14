package com.gaekdam.gaekdambe.dummy.generate.customer_service.membership;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ChangeSource;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.Customer;
import com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository.CustomerRepository;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.MembershipStatus;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity.Membership;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity.MembershipGrade;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity.MembershipHistory;
import com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository.MembershipGradeRepository;
import com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository.MembershipHistoryRepository;
import com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository.MembershipRepository;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DummyMembershipDataTest {

    @Autowired private CustomerRepository customerRepository;
    @Autowired private MembershipGradeRepository membershipGradeRepository;
    @Autowired private MembershipRepository membershipRepository;
    @Autowired private MembershipHistoryRepository membershipHistoryRepository;

    @Autowired private HotelGroupRepository hotelGroupRepository;

    private final Map<Long, List<MembershipGrade>> gradeCache = new HashMap<>();
    private static final double MEMBERSHIP_RATE = 0.70;

    //  변경: 멤버십 산정 기준(임시 JSON)
    private static final String STANDARD = "{\"windowMonths\":12}";

    private static final List<GradeSeed> GRADE_SEEDS = List.of(
            new GradeSeed("BASIC",  1L, "기본"),
            new GradeSeed("BRONZE", 2L, "브론즈"),
            new GradeSeed("SILVER", 3L, "실버"),
            new GradeSeed("GOLD",   4L, "골드"),
            new GradeSeed("VIP",    5L, "VIP")
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

        seedMembershipGradesIfNeeded(hotelGroupCodes, now);

        for (Customer customer : customers) {
            createMembershipIfNeeded(customer, now);
        }
    }

    private void seedMembershipGradesIfNeeded(List<Long> hotelGroupCodes, LocalDateTime now) {

        List<MembershipGrade> all = membershipGradeRepository.findAll();

        Map<Long, Map<String, MembershipGrade>> byHg = new HashMap<>();
        for (MembershipGrade g : all) {
            byHg.computeIfAbsent(g.getHotelGroupCode(), k -> new HashMap<>())
                    .putIfAbsent(g.getGradeName(), g);
        }

        for (Long hg : hotelGroupCodes) {
            Map<String, MembershipGrade> existing = byHg.computeIfAbsent(hg, k -> new HashMap<>());

            for (GradeSeed seed : GRADE_SEEDS) {
                if (!existing.containsKey(seed.gradeName())) {
                    MembershipGrade saved = membershipGradeRepository.save(
                            MembershipGrade.registerMembershipGrade(
                                    hg,
                                    seed.gradeName(),
                                    seed.tierLevel(),
                                    seed.tierComment(),
                                    STANDARD, // ✅ 변경: 산정 기준 추가
                                    true,
                                    now
                            )
                    );
                    existing.put(saved.getGradeName(), saved);
                }
            }

            List<MembershipGrade> grades = new ArrayList<>(existing.values());
            grades.sort(Comparator.comparing(MembershipGrade::getTierLevel));
            gradeCache.put(hg, grades);
        }
    }

    private void createMembershipIfNeeded(Customer customer, LocalDateTime now) {
        if (!chance(MEMBERSHIP_RATE)) return;

        Long customerCode = customer.getCustomerCode();
        Long hg = customer.getHotelGroupCode();

        if (membershipRepository.existsByHotelGroupCodeAndCustomerCode(hg, customerCode)) return;

        List<MembershipGrade> grades = gradeCache.get(hg);
        if (grades == null || grades.isEmpty()) return;

        MembershipGrade picked = pickMembershipGrade(grades);
        LocalDateTime joinedAt = now.minusDays(randomInt(0, 30));

        Membership membership = Membership.registerMembership(
                customerCode,
                hg,
                picked.getMembershipGradeCode(),
                joinedAt,
                now
        );
        membershipRepository.save(membership);

        membershipHistoryRepository.save(
                MembershipHistory.recordMembershipChange(
                        customerCode,
                        membership.getMembershipCode(),
                        ChangeSource.SYSTEM,
                        null,
                        "더미 등급 부여",
                        null,
                        picked.getGradeName(),
                        null,
                        MembershipStatus.ACTIVE,
                        null,
                        null,
                        joinedAt,
                        picked.getMembershipGradeCode()
                )
        );

        if (chance(0.10)) membership.changeMembershipStatus(MembershipStatus.SUSPENDED, now.plusDays(3));
        if (chance(0.05)) membership.changeMembershipStatus(MembershipStatus.EXPIRED, now.plusDays(10));
    }

    private MembershipGrade pickMembershipGrade(List<MembershipGrade> grades) {
        double r = ThreadLocalRandom.current().nextDouble();
        if (r < 0.55) return findGrade(grades, "BASIC");
        if (r < 0.75) return findGrade(grades, "BRONZE");
        if (r < 0.90) return findGrade(grades, "SILVER");
        if (r < 0.98) return findGrade(grades, "GOLD");
        return findGrade(grades, "VIP");
    }

    private MembershipGrade findGrade(List<MembershipGrade> grades, String gradeName) {
        return grades.stream()
                .filter(g -> gradeName.equals(g.getGradeName()))
                .findFirst()
                .orElseGet(() -> grades.get(ThreadLocalRandom.current().nextInt(grades.size())));
    }

    private boolean chance(double probability) {
        return ThreadLocalRandom.current().nextDouble() < probability;
    }

    private int randomInt(int minInclusive, int maxExclusive) {
        return ThreadLocalRandom.current().nextInt(minInclusive, maxExclusive);
    }

    private record GradeSeed(String gradeName, Long tierLevel, String tierComment) {}
}
