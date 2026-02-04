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

            MembershipGrade grade = scopedGrades.get(random.nextInt(scopedGrades.size()));

            LocalDateTime joinedAt = randomDateTimeBetween(START, END.minusDays(30), random);

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

            // 2. 연도 갱신 시뮬레이션 (확률적으로 다음 해, 다다음 해로 갱신)
            // joinedAt이 2024년이면 -> 2025년 1월 1일 갱신 시뮬레이션
            int currentYear = joinedAt.getYear();
            int maxYear = END.getYear(); // 2026

            // 반복적으로 해를 넘기며 갱신 (확률 50%로 다음해 연장)
            while (currentYear < maxYear && random.nextBoolean()) {
                int nextYear = currentYear + 1;
                LocalDateTime renewalDate = LocalDateTime.of(nextYear, 1, 1, 6, 38); // 배치 실행 시간

                // 2-1. 갱신 전 상태 백업 (History용)
                MembershipGrade beforeGrade = grade; // (단순화: 등급 변동 없음 가정 or 랜덤 변경)

                // 확률적으로 등급 변경 발생 (20%)
                if (random.nextInt(100) < 20) {
                    grade = scopedGrades.get(random.nextInt(scopedGrades.size()));
                }

                MembershipStatus beforeStatus = membership.getMembershipStatus();
                LocalDateTime beforeExpiredAt = membership.getExpiredAt();

                // 2-2. 갱신 후 상태 계산
                // 산정일 = 1월 1일
                // 만료일 = 12월 31일
                LocalDateTime newCalculatedAt = renewalDate; // 1월 1일
                LocalDateTime newExpiredAt = LocalDateTime.of(nextYear, 12, 31, 23, 59, 59);

                // 2-3. 멤버십 업데이트 (메모리 상 객체 수정)
                membership.changeMembership(
                        grade.getMembershipGradeCode(),
                        beforeStatus, // 상태 유지
                        newExpiredAt,
                        newCalculatedAt);

                // 2-4. 이력 저장 (갱신 시점 기록)
                MembershipHistory history = MembershipHistory.recordMembershipChange(
                        customerCode,
                        0L, // flush 후 id 주입
                        ChangeSource.SYSTEM,
                        null, // system
                        "Automatic Grade Update (Dummy Simulation)",
                        beforeGrade.getGradeName(),
                        grade.getGradeName(),
                        beforeStatus,
                        beforeStatus,
                        beforeExpiredAt,
                        newExpiredAt,
                        renewalDate,
                        grade.getMembershipGradeCode());

                historySlots.add(new HistorySlot(idxInBatch, history));

                currentYear = nextYear;
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
}
