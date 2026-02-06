package com.gaekdam.gaekdambe.dummy.generate.customer_service.loyalty;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ChangeSource;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.LoyaltyStatus;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.Loyalty;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.LoyaltyHistory;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.infrastructure.repository.LoyaltyHistoryRepository;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.infrastructure.repository.LoyaltyRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class DummyLoyaltyDataTest {

    private static final int BATCH = 500;

    // 승급 비율: 15%면 대략 15%가 EXCELLENT로 저장됨
    private static final int EXCELLENT_PROMOTION_RATE = 15;

    private static final LocalDateTime START = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 12, 31, 23, 59);

    @Autowired
    LoyaltyRepository loyaltyRepository;
    @Autowired
    LoyaltyHistoryRepository loyaltyHistoryRepository;

    @PersistenceContext
    EntityManager em;

    @Transactional
    public void generate() {

        // 이미 loyalty 있으면 스킵(다시 만들고 싶으면 loyalty/loyalty_history 비우고 실행)
        if (loyaltyRepository.count() > 0)
            return;

        // 호텔그룹별 GENERAL/EXCELLENT 등급코드 매핑
        Map<Long, GradePair> gradeByHotelGroup = loadGradePairsByHotelGroup();
        if (gradeByHotelGroup.isEmpty())
            return;

        List<Object[]> customerRows = loadCustomerHotelGroupRows();
        if (customerRows.isEmpty())
            return;

        Map<Long, List<Long>> employeeByHotelGroup = loadActiveEmployeeCodesByHotelGroup();
        List<Long> fallbackEmployees = new ArrayList<>();
        for (List<Long> list : employeeByHotelGroup.values()) {
            fallbackEmployees.addAll(list);
        }
        fallbackEmployees = fallbackEmployees.stream().distinct().toList();
        if (fallbackEmployees.isEmpty())
            return;

        Random random = new Random();

        List<Loyalty> loyaltyBuffer = new ArrayList<>(BATCH);
        List<HistorySlot> historySlots = new ArrayList<>(BATCH);

        for (Object[] row : customerRows) {
            long customerCode = ((Number) row[0]).longValue();
            long hotelGroupCode = ((Number) row[1]).longValue();

            GradePair pair = gradeByHotelGroup.get(hotelGroupCode);
            if (pair == null || pair.generalCode == null)
                continue;

            LocalDateTime joinedAt = randomDateTimeBetween(START, END.minusDays(10), random);
            LocalDateTime now = joinedAt;

            // 기본은 GENERAL
            // [단순화] 랜덤 방문 횟수 기반 1회 생성 (0~30회)
            long randomVisitCount = random.nextInt(31);
            long visitThreshold = 5;

            Long initialGradeCode = pair.generalCode;
            if (pair.excellentCode != null && randomVisitCount >= visitThreshold) {
                initialGradeCode = pair.excellentCode;
            }

            // Loyalty 생성
            Loyalty loyalty = Loyalty.registerLoyalty(
                    customerCode,
                    hotelGroupCode,
                    initialGradeCode,
                    joinedAt,
                    joinedAt);

            int idxInBatch = loyaltyBuffer.size();
            loyaltyBuffer.add(loyalty);

            // 초기 이력 저장
            LoyaltyHistory history = LoyaltyHistory.recordLoyaltyChange(
                    customerCode,
                    0L, // flush 후 설정
                    ChangeSource.SYSTEM,
                    null,
                    "Initial Loyalty Grade (Simulated Visits: " + randomVisitCount + ")",
                    null,
                    initialGradeCode,
                    LoyaltyStatus.ACTIVE,
                    LoyaltyStatus.ACTIVE,
                    joinedAt);

            historySlots.add(new HistorySlot(idxInBatch, history));

            if (loyaltyBuffer.size() == BATCH) {
                flushBatch(loyaltyBuffer, historySlots);
            }
        }

        if (!loyaltyBuffer.isEmpty()) {
            flushBatch(loyaltyBuffer, historySlots);
        }
    }

    private void flushBatch(List<Loyalty> loyaltyBuffer, List<HistorySlot> historySlots) {

        loyaltyRepository.saveAll(loyaltyBuffer);
        em.flush();

        List<LoyaltyHistory> historyBuffer = new ArrayList<>(historySlots.size());
        for (HistorySlot slot : historySlots) {
            Loyalty saved = loyaltyBuffer.get(slot.loyaltyIndex);
            setLoyaltyCode(slot.history, saved.getLoyaltyCode());
            historyBuffer.add(slot.history);
        }

        if (!historyBuffer.isEmpty()) {
            loyaltyHistoryRepository.saveAll(historyBuffer);
        }

        em.flush();
        em.clear();

        loyaltyBuffer.clear();
        historySlots.clear();
    }

    private record HistorySlot(int loyaltyIndex, LoyaltyHistory history) {
    }

    private record GradePair(Long generalCode, Long excellentCode) {
    }

    private Map<Long, GradePair> loadGradePairsByHotelGroup() {

        List<?> raw = em.createNativeQuery(
                        "select hotel_group_code, loyalty_grade_name, loyalty_grade_code " +
                                "  from loyalty_grade " +
                                " where loyalty_grade_status = 'ACTIVE'")
                .getResultList();

        Map<Long, Long> generalMap = new HashMap<>();
        Map<Long, Long> excellentMap = new HashMap<>();

        for (Object obj : raw) {
            Object[] r = (Object[]) obj;

            Long hg = ((Number) r[0]).longValue();
            String name = (String) r[1];
            Long code = ((Number) r[2]).longValue();

            if ("GENERAL".equalsIgnoreCase(name))
                generalMap.put(hg, code);
            if ("EXCELLENT".equalsIgnoreCase(name))
                excellentMap.put(hg, code);
        }

        Map<Long, GradePair> out = new HashMap<>();
        for (Long hg : generalMap.keySet()) {
            out.put(hg, new GradePair(generalMap.get(hg), excellentMap.get(hg)));
        }
        return out;
    }

    private List<Object[]> loadCustomerHotelGroupRows() {

        List<?> raw = em.createNativeQuery(
                        "select customer_code, hotel_group_code " +
                                "  from customer")
                .getResultList();

        List<Object[]> out = new ArrayList<>(raw.size());
        for (Object obj : raw) {
            out.add((Object[]) obj);
        }
        return out;
    }

    private Map<Long, List<Long>> loadActiveEmployeeCodesByHotelGroup() {

        List<?> raw = em.createNativeQuery(
                        "select hotel_group_code, employee_code " +
                                "  from employee " +
                                " where employee_status = 'ACTIVE' " +
                                "   and hotel_group_code is not null")
                .getResultList();

        Map<Long, List<Long>> map = new HashMap<>();
        for (Object obj : raw) {
            Object[] r = (Object[]) obj;
            Long hg = ((Number) r[0]).longValue();
            Long ec = ((Number) r[1]).longValue();
            map.computeIfAbsent(hg, k -> new ArrayList<>()).add(ec);
        }

        for (List<Long> list : map.values()) {
            list.sort(Comparator.naturalOrder());
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

    private static void setLoyaltyCode(LoyaltyHistory h, long loyaltyCode) {
        try {
            var f = LoyaltyHistory.class.getDeclaredField("loyaltyCode");
            f.setAccessible(true);
            f.set(h, loyaltyCode);
        } catch (Exception e) {
            throw new RuntimeException("LoyaltyHistory.loyaltyCode set failed", e);
        }
    }
}
