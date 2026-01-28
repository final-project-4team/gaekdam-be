package com.gaekdam.gaekdambe.dummy.generate.customer_service.loyalty;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ChangeSource;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.LoyaltyStatus;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.Loyalty;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.LoyaltyGrade;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.LoyaltyHistory;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.infrastructure.repository.LoyaltyGradeRepository;
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

    // EXCELLENT 승급 이력 비율 (원하는대로 조절)
    private static final int EXCELLENT_PROMOTION_RATE = 15; // %

    private static final LocalDateTime START = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 12, 31, 23, 59);

    @Autowired LoyaltyRepository loyaltyRepository;
    @Autowired LoyaltyGradeRepository loyaltyGradeRepository;
    @Autowired LoyaltyHistoryRepository loyaltyHistoryRepository;

    @PersistenceContext
    EntityManager em;

    @Transactional
    public void generate() {

        if (loyaltyRepository.count() > 0) return;

        List<LoyaltyGrade> grades = loyaltyGradeRepository.findAll();
        if (grades.isEmpty()) return;

        // GENERAL = 가장 먼저 생성된 등급(보통 PK가 가장 작음)
        LoyaltyGrade general = grades.stream()
                .min(Comparator.comparing(LoyaltyGrade::getLoyaltyGradeCode))
                .orElse(null);
        if (general == null) return;

        // EXCELLENT = 다음 등급(없으면 승급 이력 생성 안 함)
        LoyaltyGrade excellent = (grades.size() >= 2)
                ? grades.stream().max(Comparator.comparing(LoyaltyGrade::getLoyaltyGradeCode)).orElse(null)
                : null;

        List<Object[]> customerRows = loadCustomerHotelGroupRows();
        if (customerRows.isEmpty()) return;

        Map<Long, List<Long>> employeeByHotelGroup = loadActiveEmployeeCodesByHotelGroup();
        List<Long> fallbackEmployees = employeeByHotelGroup.values().stream()
                .flatMap(List::stream)
                .distinct()
                .toList();
        if (fallbackEmployees.isEmpty()) return;

        Random random = new Random();

        List<Loyalty> loyaltyBuffer = new ArrayList<>(BATCH);
        List<HistorySlot> historySlots = new ArrayList<>(BATCH);

        for (Object[] row : customerRows) {
            long customerCode = ((Number) row[0]).longValue();
            long hotelGroupCode = ((Number) row[1]).longValue();

            LocalDateTime joinedAt = randomDateTimeBetween(START, END.minusDays(10), random);
            LocalDateTime now = joinedAt;

            // 초기 등급은 무조건 GENERAL
            Loyalty loyalty = Loyalty.registerLoyalty(
                    customerCode,
                    hotelGroupCode,
                    general.getLoyaltyGradeCode(),
                    joinedAt,
                    now
            );

            int idxInBatch = loyaltyBuffer.size();
            loyaltyBuffer.add(loyalty);

            // 일부만 EXCELLENT 승급 이력 생성
            if (excellent != null && !Objects.equals(excellent.getLoyaltyGradeCode(), general.getLoyaltyGradeCode())
                    && random.nextInt(100) < EXCELLENT_PROMOTION_RATE) {

                LocalDateTime changedAt = joinedAt.plusDays(5 + random.nextInt(180));
                if (changedAt.isAfter(END)) changedAt = END;

                Long employeeCode = pickEmployeeCode(hotelGroupCode, employeeByHotelGroup, fallbackEmployees, random);

                LoyaltyHistory history = LoyaltyHistory.recordLoyaltyChange(
                        customerCode,
                        0L, // flush 후 주입
                        ChangeSource.SYSTEM,
                        employeeCode,
                        "dummy loyalty promotion",
                        general.getLoyaltyGradeCode(),
                        excellent.getLoyaltyGradeCode(),
                        LoyaltyStatus.ACTIVE,
                        LoyaltyStatus.ACTIVE,
                        changedAt
                );

                historySlots.add(new HistorySlot(idxInBatch, history));
            }

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

    private Long pickEmployeeCode(
            Long hotelGroupCode,
            Map<Long, List<Long>> employeeByHotelGroup,
            List<Long> fallbackEmployees,
            Random random
    ) {
        List<Long> scoped = employeeByHotelGroup.get(hotelGroupCode);
        if (scoped != null && !scoped.isEmpty()) {
            return scoped.get(random.nextInt(scoped.size()));
        }
        return fallbackEmployees.get(random.nextInt(fallbackEmployees.size()));
    }

    private List<Object[]> loadCustomerHotelGroupRows() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("""
                select customer_code, hotel_group_code
                  from customer
                """).getResultList();
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
