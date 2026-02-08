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

    private static final LocalDateTime START = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime END   = LocalDateTime.of(2026, 12, 31, 23, 59);

    private static final long VISIT_THRESHOLD = 5;

    @Autowired
    LoyaltyRepository loyaltyRepository;

    @Autowired
    LoyaltyHistoryRepository loyaltyHistoryRepository;

    @PersistenceContext
    EntityManager em;

    @Transactional
    public void generate() {

        if (loyaltyRepository.count() > 0) return;

        Map<Long, GradePair> gradeByHotelGroup = loadGradePairsByHotelGroup();
        if (gradeByHotelGroup.isEmpty()) return;

        List<Object[]> customerRows = loadCustomerHotelGroupRows();
        if (customerRows.isEmpty()) return;

        Map<Long, Long> visitCountByCustomer = loadVisitCountLast12Months();

        Random random = new Random();

        List<Loyalty> loyaltyBuffer = new ArrayList<>(BATCH);
        List<HistorySlot> historySlots = new ArrayList<>(BATCH);

        for (Object[] row : customerRows) {
            long customerCode = ((Number) row[0]).longValue();
            long hotelGroupCode = ((Number) row[1]).longValue();

            GradePair pair = gradeByHotelGroup.get(hotelGroupCode);
            if (pair == null || pair.generalCode == null) continue;

            LocalDateTime joinedAt = randomDateTimeBetween(START, END.minusDays(30), random);

            long visitCount = visitCountByCustomer.getOrDefault(customerCode, 0L);

            Long initialGradeCode = pair.generalCode;
            if (pair.excellentCode != null && visitCount >= VISIT_THRESHOLD) {
                initialGradeCode = pair.excellentCode;
            }

            Loyalty loyalty = Loyalty.registerLoyalty(
                    customerCode,
                    hotelGroupCode,
                    initialGradeCode,
                    joinedAt,
                    joinedAt
            );

            int idxInBatch = loyaltyBuffer.size();
            loyaltyBuffer.add(loyalty);

            LoyaltyHistory history = LoyaltyHistory.recordLoyaltyChange(
                    customerCode,
                    0L,
                    ChangeSource.SYSTEM,
                    null,
                    "Initial Loyalty Grade (Visits last 12 months: " + visitCount + ")",
                    null,
                    initialGradeCode,
                    LoyaltyStatus.ACTIVE,
                    LoyaltyStatus.ACTIVE,
                    joinedAt
            );

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

    private record HistorySlot(int loyaltyIndex, LoyaltyHistory history) {}
    private record GradePair(Long generalCode, Long excellentCode) {}

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

            if ("GENERAL".equalsIgnoreCase(name)) generalMap.put(hg, code);
            if ("EXCELLENT".equalsIgnoreCase(name)) excellentMap.put(hg, code);
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
        for (Object obj : raw) out.add((Object[]) obj);
        return out;
    }

    private Map<Long, Long> loadVisitCountLast12Months() {

        LocalDateTime statsEnd = LocalDateTime.now();
        LocalDateTime statsStart = statsEnd.minusMonths(12);

        String sql = """
            SELECT
              s.customer_code,
              COUNT(*) AS visit_count
            FROM stay s
            WHERE s.stay_status = 'COMPLETED'
              AND s.actual_checkout_at BETWEEN ?1 AND ?2
            GROUP BY s.customer_code
            """;

        @SuppressWarnings("unchecked")
        List<Object[]> raw = em.createNativeQuery(sql)
                .setParameter(1, java.sql.Timestamp.valueOf(statsStart))
                .setParameter(2, java.sql.Timestamp.valueOf(statsEnd))
                .getResultList();

        Map<Long, Long> map = new HashMap<>(raw.size() * 2);
        for (Object[] r : raw) {
            long customerCode = ((Number) r[0]).longValue();
            long visitCount = ((Number) r[1]).longValue();
            map.put(customerCode, visitCount);
        }
        return map;
    }

    private static LocalDateTime randomDateTimeBetween(LocalDateTime start, LocalDateTime end, Random random) {
        long seconds = Duration.between(start, end).getSeconds();
        if (seconds <= 0) return start;
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
