package com.gaekdam.gaekdambe.dummy.generate.customer_service.membership;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ChangeSource;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.Customer;
import com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository.CustomerRepository;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.MembershipGradeName;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.MembershipStatus;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity.*;
import com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository.MembershipGradeRepository;
import com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository.MembershipHistoryRepository;
import com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository.MembershipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Component
public class DummyMembershipDataTest {

    @Autowired private CustomerRepository customerRepository;
    @Autowired private MembershipGradeRepository membershipGradeRepository;
    @Autowired private MembershipRepository membershipRepository;
    @Autowired private MembershipHistoryRepository membershipHistoryRepository;

    private final Map<Long, List<MembershipGrade>> gradeCache = new HashMap<>();
    private static final double MEMBERSHIP_RATE = 0.70;

    @Transactional
    public void generate() {

        List<Customer> customers = customerRepository.findAll();
        if (customers.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();

        List<Long> hotelGroupCodes = customers.stream()
                .map(Customer::getHotelGroupCode)
                .distinct()
                .collect(Collectors.toList());

        seedMembershipGradesIfNeeded(hotelGroupCodes, now);

        for (Customer customer : customers) {
            createMembershipIfNeeded(customer, now);
        }
    }

    private void seedMembershipGradesIfNeeded(List<Long> hotelGroupCodes, LocalDateTime now) {

        if (hotelGroupCodes == null || hotelGroupCodes.isEmpty()) {
            throw new IllegalArgumentException("hotelGroupCodes is empty");
        }

        List<MembershipGrade> all = membershipGradeRepository.findAll();

        Map<MembershipGradeName, MembershipGrade> unique = new EnumMap<>(MembershipGradeName.class);
        for (MembershipGrade g : all) {
            unique.putIfAbsent(g.getGradeName(), g);
        }

        Long seedHotelGroupCode = hotelGroupCodes.get(0);

        unique.putIfAbsent(MembershipGradeName.BASIC,
                membershipGradeRepository.save(
                        MembershipGrade.registerMembershipGrade(seedHotelGroupCode, MembershipGradeName.BASIC, 1L, "기본", true, now)
                )
        );
        unique.putIfAbsent(MembershipGradeName.BRONZE,
                membershipGradeRepository.save(
                        MembershipGrade.registerMembershipGrade(seedHotelGroupCode, MembershipGradeName.BRONZE, 2L, "브론즈", true, now)
                )
        );
        unique.putIfAbsent(MembershipGradeName.SILVER,
                membershipGradeRepository.save(
                        MembershipGrade.registerMembershipGrade(seedHotelGroupCode, MembershipGradeName.SILVER, 3L, "실버", true, now)
                )
        );
        unique.putIfAbsent(MembershipGradeName.GOLD,
                membershipGradeRepository.save(
                        MembershipGrade.registerMembershipGrade(seedHotelGroupCode, MembershipGradeName.GOLD, 4L, "골드", true, now)
                )
        );
        unique.putIfAbsent(MembershipGradeName.VIP,
                membershipGradeRepository.save(
                        MembershipGrade.registerMembershipGrade(seedHotelGroupCode, MembershipGradeName.VIP, 5L, "VIP", true, now)
                )
        );

        List<MembershipGrade> grades = new ArrayList<>(unique.values());
        grades.sort(Comparator.comparing(MembershipGrade::getTierLevel));

        for (Long hg : hotelGroupCodes) {
            gradeCache.put(hg, grades);
        }
    }

    private void createMembershipIfNeeded(Customer customer, LocalDateTime now) {
        if (!chance(MEMBERSHIP_RATE)) return;

        Long customerCode = customer.getCustomerCode();
        Long hg = customer.getHotelGroupCode();

        if (membershipRepository.existsByHotelGroupCodeAndCustomerCode(hg, customerCode)) return;

        List<MembershipGrade> grades = gradeCache.get(hg);
        if (grades == null || grades.isEmpty()) {
            throw new IllegalStateException("MembershipGrade not seeded for hotelGroupCode=" + hg);
        }

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
                        picked.getGradeName().name(),
                        null,
                        MembershipStatus.ACTIVE,
                        null,
                        null,
                        joinedAt,
                        picked.getMembershipGradeCode()
                )
        );

        if (chance(0.20)) {
            MembershipGrade after = pickMembershipGrade(grades);

            membershipHistoryRepository.save(
                    MembershipHistory.recordMembershipChange(
                            customerCode,
                            membership.getMembershipCode(),
                            ChangeSource.MANUAL,
                            1L,
                            "더미 등급 변경",
                            picked.getGradeName().name(),
                            after.getGradeName().name(),
                            MembershipStatus.ACTIVE,
                            MembershipStatus.ACTIVE,
                            null,
                            null,
                            now.plusDays(7),
                            after.getMembershipGradeCode()
                    )
            );
        }
    }

    private MembershipGrade pickMembershipGrade(List<MembershipGrade> grades) {
        double r = ThreadLocalRandom.current().nextDouble();
        if (r < 0.55) return findGrade(grades, MembershipGradeName.BASIC);
        if (r < 0.75) return findGrade(grades, MembershipGradeName.BRONZE);
        if (r < 0.90) return findGrade(grades, MembershipGradeName.SILVER);
        if (r < 0.98) return findGrade(grades, MembershipGradeName.GOLD);
        return findGrade(grades, MembershipGradeName.VIP);
    }

    private MembershipGrade findGrade(List<MembershipGrade> grades, MembershipGradeName name) {
        return grades.stream()
                .filter(g -> g.getGradeName() == name)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("MembershipGrade not found: " + name));
    }

    private boolean chance(double probability) {
        return ThreadLocalRandom.current().nextDouble() < probability;
    }

    private int randomInt(int minInclusive, int maxExclusive) {
        return ThreadLocalRandom.current().nextInt(minInclusive, maxExclusive);
    }
}
