package com.gaekdam.gaekdambe.dummy.generate.customer_service.membership;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ChangeSource;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.MembershipStatus;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity.Membership;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity.MembershipGrade;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity.MembershipHistory;
import com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository.MembershipGradeRepository;
import com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository.MembershipHistoryRepository;
import com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository.MembershipRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class DummyMembershipDataTest {

    private static final int BATCH = 500;

    private static final LocalDateTime START = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 12, 31, 23, 59);

    private static final int MEMBERSHIP_RATE_PERCENT = 35;

    @Autowired MembershipRepository membershipRepository;
    @Autowired MembershipGradeRepository membershipGradeRepository;
    @Autowired MembershipHistoryRepository membershipHistoryRepository;

    @PersistenceContext
    EntityManager em;

    @Transactional
    public void generate() {

        // ✅ memberRepository 체크 제거 (member는 별도 후처리 더미에서 생성)
        if (membershipRepository.count() > 0) return;

        List<MembershipGrade> grades = membershipGradeRepository.findAll();
        if (grades.isEmpty()) return;

        List<Object[]> customerRows = loadCustomerHotelGroupRows();
        if (customerRows.isEmpty()) return;

        Map<Long, List<Long>> employeeByHotelGroup = loadActiveEmployeeCodesByHotelGroup();
        List<Long> fallbackEmployees = employeeByHotelGroup.values().stream()
                .flatMap(List::stream)
                .distinct()
                .toList();
        if (fallbackEmployees.isEmpty()) return;

        Random random = new Random();

        List<Membership> membershipBuffer = new ArrayList<>(BATCH);
        List<HistorySlot> historySlots = new ArrayList<>(BATCH);

        for (Object[] row : customerRows) {
            long customerCode = ((Number) row[0]).longValue();
            long hotelGroupCode = ((Number) row[1]).longValue();

            if (random.nextInt(100) >= MEMBERSHIP_RATE_PERCENT) continue;

            LocalDateTime joinedAt = randomDateTimeBetween(START, END.minusDays(30), random);
            LocalDateTime now = joinedAt;

            MembershipGrade grade = grades.get(random.nextInt(grades.size()));

            Membership membership = Membership.registerMembership(
                    customerCode,
                    hotelGroupCode,
                    grade.getMembershipGradeCode(),
                    joinedAt,
                    now
            );

            int idxInBatch = membershipBuffer.size();
            membershipBuffer.add(membership);

            // 히스토리 20%
            if (random.nextInt(100) < 20) {
                MembershipGrade afterGrade = grades.get(random.nextInt(grades.size()));
                MembershipStatus beforeStatus = MembershipStatus.ACTIVE;
                MembershipStatus afterStatus =
                        (random.nextInt(100) < 10) ? MembershipStatus.SUSPENDED : MembershipStatus.ACTIVE;

                LocalDateTime changedAt = joinedAt.plusDays(10 + random.nextInt(200));
                if (changedAt.isAfter(END)) changedAt = END;

                Long employeeCode = pickEmployeeCode(hotelGroupCode, employeeByHotelGroup, fallbackEmployees, random);

                MembershipHistory history = MembershipHistory.recordMembershipChange(
                        customerCode,
                        0L, // flush 후 주입
                        ChangeSource.SYSTEM,
                        employeeCode,
                        "dummy membership change",
                        grade.getGradeName(),
                        afterGrade.getGradeName(),
                        beforeStatus,
                        afterStatus,
                        null,
                        null,
                        changedAt,
                        afterGrade.getMembershipGradeCode()
                );

                historySlots.add(new HistorySlot(idxInBatch, history));
            }

            if (membershipBuffer.size() == BATCH) {
                flushBatch(membershipBuffer, historySlots);
            }
        }

        if (!membershipBuffer.isEmpty()) {
            flushBatch(membershipBuffer, historySlots);
        }
    }

    private void flushBatch(List<Membership> membershipBuffer, List<HistorySlot> historySlots) {
        membershipRepository.saveAll(membershipBuffer);
        em.flush();

        List<MembershipHistory> historyBuffer = new ArrayList<>(historySlots.size());
        for (HistorySlot slot : historySlots) {
            Membership saved = membershipBuffer.get(slot.membershipIndex);
            setMembershipCode(slot.history, saved.getMembershipCode());
            historyBuffer.add(slot.history);
        }

        if (!historyBuffer.isEmpty()) membershipHistoryRepository.saveAll(historyBuffer);

        em.flush();
        em.clear();

        membershipBuffer.clear();
        historySlots.clear();
    }

    private record HistorySlot(int membershipIndex, MembershipHistory history) {}

    private Long pickEmployeeCode(
            Long hotelGroupCode,
            Map<Long, List<Long>> employeeByHotelGroup,
            List<Long> fallbackEmployees,
            Random random
    ) {
        List<Long> scoped = employeeByHotelGroup.get(hotelGroupCode);
        if (scoped != null && !scoped.isEmpty()) return scoped.get(random.nextInt(scoped.size()));
        return fallbackEmployees.get(random.nextInt(fallbackEmployees.size()));
    }

    private List<Object[]> loadCustomerHotelGroupRows() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("select customer_code, hotel_group_code from customer").getResultList();
        return rows;
    }

    private Map<Long, List<Long>> loadActiveEmployeeCodesByHotelGroup() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("""
                select hotel_group_code, employee_code
                  from employee
                 where employee_status = 'ACTIVE'
                   and hotel_group_code is not null
                """).getResultList();

        Map<Long, List<Long>> map = new HashMap<>();
        for (Object[] r : rows) {
            Long hg = ((Number) r[0]).longValue();
            Long ec = ((Number) r[1]).longValue();
            map.computeIfAbsent(hg, k -> new ArrayList<>()).add(ec);
        }
        map.values().forEach(list -> list.sort(Comparator.naturalOrder()));
        return map;
    }

    private static LocalDateTime randomDateTimeBetween(LocalDateTime start, LocalDateTime end, Random random) {
        long seconds = Duration.between(start, end).getSeconds();
        if (seconds <= 0) return start;
        long add = (random.nextLong() & Long.MAX_VALUE) % seconds;
        return start.plusSeconds(add);
    }

    private static void setMembershipCode(MembershipHistory h, long membershipCode) {
        try {
            var f = MembershipHistory.class.getDeclaredField("membershipCode");
            f.setAccessible(true);
            f.set(h, membershipCode);
        } catch (Exception e) {
            throw new RuntimeException("MembershipHistory.membershipCode set failed", e);
        }
    }
}
