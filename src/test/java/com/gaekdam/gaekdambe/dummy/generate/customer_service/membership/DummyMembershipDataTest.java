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

    // 정책: 산정기간 12개월 고정 (관리자 설정으로 바꾸려면 grade.getCalculationTermMonth() 사용)
    private static final int DEFAULT_TERM_MONTHS = 12;

    @Autowired
    MembershipRepository membershipRepository;
    @Autowired
    MembershipGradeRepository membershipGradeRepository;
    @Autowired
    MembershipHistoryRepository membershipHistoryRepository;

    @PersistenceContext
    EntityManager em;

    @Transactional
    public void generate() {

        if (membershipRepository.count() > 0)
            return;

        List<MembershipGrade> grades = membershipGradeRepository.findAll();
        if (grades.isEmpty())
            return;

        Map<Long, List<MembershipGrade>> gradesByHotelGroup = groupGradesByHotelGroup(grades);

        List<Object[]> customerRows = loadCustomerHotelGroupRows();
        if (customerRows.isEmpty())
            return;

        Map<Long, List<Long>> employeeByHotelGroup = loadActiveEmployeeCodesByHotelGroup();
        List<Long> fallbackEmployees = employeeByHotelGroup.values().stream()
                .flatMap(List::stream)
                .distinct()
                .toList();
        if (fallbackEmployees.isEmpty())
            return;

        Random random = new Random();

        List<Membership> membershipBuffer = new ArrayList<>(BATCH);
        List<HistorySlot> historySlots = new ArrayList<>(BATCH);

        for (Object[] row : customerRows) {
            long customerCode = ((Number) row[0]).longValue();
            long hotelGroupCode = ((Number) row[1]).longValue();

            if (random.nextInt(100) >= MEMBERSHIP_RATE_PERCENT)
                continue;

            List<MembershipGrade> scopedGrades = gradesByHotelGroup.get(hotelGroupCode);
            if (scopedGrades == null || scopedGrades.isEmpty())
                continue;

            LocalDateTime joinedAt = randomDateTimeBetween(START, END.minusDays(30), random);

            // 초기 가입: 최하위 등급으로 시작 (실제 호텔 멤버십 정책과 동일)
            // 이후 연도 갱신 시 실적에 따라 등급이 상승합니다
            MembershipGrade grade = scopedGrades.stream()
                    .min(Comparator.comparing(MembershipGrade::getTierLevel))
                    .orElse(scopedGrades.get(0));

            // 1. 가입 시점 세팅
            // 산정일 = 가입일
            // 만료일 = 가입년도 12월 31일
            LocalDateTime calculatedAt = joinedAt;
            LocalDateTime expiredAt = LocalDateTime.of(joinedAt.getYear(), 12, 31, 23, 59, 59);

            Membership membership = Membership.registerMembership(
                    customerCode,
                    hotelGroupCode,
                    grade.getMembershipGradeCode(),
                    joinedAt,
                    joinedAt);

            // DB에 초기 상태 반영 (가입 시점)
            membership.changeMembership(
                    grade.getMembershipGradeCode(),
                    MembershipStatus.ACTIVE,
                    expiredAt,
                    calculatedAt);

            int idxInBatch = membershipBuffer.size();
            membershipBuffer.add(membership);

            // 2. 연도 갱신 시뮬레이션
            // joinedAt이 2024년이면 -> 2025년 1월 1일 갱신 시뮬레이션
            int currentYear = joinedAt.getYear();
            int maxYear = END.getYear(); // 2026

            // 매년 1월 1일에 갱신 시도 (랜덤 스킵 없이 수행하여 등급 정합성 보장)
            while (currentYear < maxYear) {
                int nextYear = currentYear + 1;
                LocalDateTime renewalDate = LocalDateTime.of(nextYear, 1, 1, 6, 38);

                // 2-1. 갱신 전 상태 백업 (History용)
                MembershipGrade beforeGrade = grade;

                // 갱신 시점의 실적을 기준으로 등급 재계산
                grade = calculateBestGradeForCustomer(customerCode, scopedGrades, renewalDate);

                MembershipStatus beforeStatus = membership.getMembershipStatus();
                LocalDateTime beforeExpiredAt = membership.getExpiredAt();

                // 2-2. 갱신 후 상태 계산 (1월 1일 ~ 12월 31일)
                LocalDateTime newCalculatedAt = renewalDate;
                LocalDateTime newExpiredAt = LocalDateTime.of(nextYear, 12, 31, 23, 59, 59);

                // 등급이 변경되었거나, 해가 바뀌었으므로 갱신 처리
                membership.changeMembership(
                        grade.getMembershipGradeCode(),
                        beforeStatus,
                        newExpiredAt,
                        newCalculatedAt);

                // 2-3. 이력 저장
                MembershipHistory history = MembershipHistory.recordMembershipChange(
                        customerCode,
                        0L,
                        ChangeSource.SYSTEM,
                        null,
                        "Automatic Grade Update based on performance",
                        beforeGrade.getGradeName(),
                        grade.getGradeName(),
                        beforeStatus,
                        beforeStatus,
                        beforeExpiredAt,
                        newExpiredAt,
                        renewalDate,
                        grade.getMembershipGradeCode());

                historySlots.add(new HistorySlot(idxInBatch, history));

                currentYear++;
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

        if (!historyBuffer.isEmpty())
            membershipHistoryRepository.saveAll(historyBuffer);

        em.flush();
        em.clear();

        membershipBuffer.clear();
        historySlots.clear();
    }

    private record HistorySlot(int membershipIndex, MembershipHistory history) {
    }

    private Long pickEmployeeCode(
            Long hotelGroupCode,
            Map<Long, List<Long>> employeeByHotelGroup,
            List<Long> fallbackEmployees,
            Random random) {
        List<Long> scoped = employeeByHotelGroup.get(hotelGroupCode);
        if (scoped != null && !scoped.isEmpty())
            return scoped.get(random.nextInt(scoped.size()));
        return fallbackEmployees.get(random.nextInt(fallbackEmployees.size()));
    }

    private List<Object[]> loadCustomerHotelGroupRows() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("select customer_code, hotel_group_code from customer")
                .getResultList();
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

    private Map<Long, List<MembershipGrade>> groupGradesByHotelGroup(List<MembershipGrade> grades) {
        Map<Long, List<MembershipGrade>> map = new HashMap<>();
        for (MembershipGrade g : grades) {
            if (g == null || g.getHotelGroup() == null || g.getHotelGroup().getHotelGroupCode() == null)
                continue;
            map.computeIfAbsent(g.getHotelGroup().getHotelGroupCode(), k -> new ArrayList<>()).add(g);
        }
        return map;
    }

    private static LocalDateTime randomDateTimeBetween(LocalDateTime start, LocalDateTime end, Random random) {
        long seconds = Duration.between(start, end).getSeconds();
        if (seconds <= 0)
            return start;
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

    /**
     * 고객의 실제 지출 통계를 기준으로 적절한 멤버십 등급을 계산합니다.
     * Reservation(Total) + Facility(Usage) for COMPLETED stays
     */
    private MembershipGrade calculateBestGradeForCustomer(
            Long customerCode,
            List<MembershipGrade> scopedGrades,
            LocalDateTime referenceDate) {

        // 등급 계산 기준 기간: 최근 12개월 (referenceDate 기준)
        LocalDateTime startDate = referenceDate.minusMonths(12);
        LocalDateTime endDate = referenceDate; // 혹은 minusDays(1)

        // 고객의 지출 통계 조회 (투숙 완료된 건에 한함: 객실/패키지 + 부대시설)
        // LTV 산출 로직과 동일하게 구성
        @SuppressWarnings("unchecked")
        List<Object[]> stats = em.createNativeQuery("""
                SELECT
                  (
                    SELECT COALESCE(SUM(r.total_price), 0)
                    FROM reservation r
                    JOIN stay s ON r.reservation_code = s.reservation_code
                    WHERE r.customer_code = ?1
                      AND s.stay_status = 'COMPLETED'
                      AND s.actual_checkout_at BETWEEN ?2 AND ?3
                  )
                  +
                  (
                    SELECT COALESCE(SUM(fu.usage_price), 0)
                    FROM facility_usage fu
                    JOIN stay s ON fu.stay_code = s.stay_code
                    WHERE s.customer_code = ?1
                      AND s.stay_status = 'COMPLETED'
                      AND s.actual_checkout_at BETWEEN ?2 AND ?3
                  ) as totalAmount,

                  (
                    SELECT COUNT(s.stay_code)
                    FROM stay s
                    WHERE s.customer_code = ?1
                      AND s.stay_status = 'COMPLETED'
                      AND s.actual_checkout_at BETWEEN ?2 AND ?3
                  ) as visitCount
                """)
                .setParameter(1, customerCode)
                .setParameter(2, startDate.toLocalDate().toString())
                .setParameter(3, endDate.toLocalDate().toString())
                .getResultList();

        java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
        Long visitCount = 0L;

        if (!stats.isEmpty()) {
            Object[] row = stats.get(0);
            if (row[0] != null)
                totalAmount = (java.math.BigDecimal) row[0];
            if (row[1] != null)
                visitCount = ((Number) row[1]).longValue();
        }

        // 등급 리스트를 티어 높은 순으로 정렬 (높은 등급부터 검사)
        scopedGrades.sort(Comparator.comparing(MembershipGrade::getTierLevel).reversed());

        // [데이터 정합성 보장]
        // 실제 지출액(totalAmount)이 해당 등급의 기준액(CalculationAmount) 이상이면 그 등급 부여
        // "가상 지출액(Virtual Boost)" 로직 제거 -> 금액과 등급의 불일치 해결

        for (MembershipGrade grade : scopedGrades) {
            Long threshold = grade.getCalculationAmount();

            // 기준액이 없으면(null) 보통 최하위거나 조건 없는 등급 -> 바로 부여 가능
            if (threshold == null) {
                return grade;
            }

            // 내 실적이 기준액 이상인가?
            if (totalAmount.compareTo(java.math.BigDecimal.valueOf(threshold)) >= 0) {
                return grade;
            }
        }

        // 조건 미달 시 최하위 등급 반환 (Logic상 마지막이 최하위)
        return scopedGrades.get(scopedGrades.size() - 1);
    }
}
