package com.gaekdam.gaekdambe.dummy.generate.customer_service.loyalty;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ChangeSource;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.LoyaltyStatus;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.Loyalty;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.LoyaltyGrade;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.LoyaltyHistory;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.infrastructure.repository.LoyaltyGradeRepository;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.infrastructure.repository.LoyaltyHistoryRepository;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.infrastructure.repository.LoyaltyRepository;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
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
    private static final LocalDateTime END = LocalDateTime.of(2026, 12, 31, 23, 59);

    @Autowired LoyaltyRepository loyaltyRepository;
    @Autowired LoyaltyGradeRepository loyaltyGradeRepository;
    @Autowired LoyaltyHistoryRepository loyaltyHistoryRepository;

    @Autowired EmployeeRepository employeeRepository;

    @PersistenceContext
    EntityManager em;

    @Transactional
    public void generate() {

        if (loyaltyRepository.count() > 0) return;

        List<LoyaltyGrade> grades = loyaltyGradeRepository.findAll();
        if (grades.isEmpty()) return;

        List<Long> employeeIds = loadIds("select employee_code from employee");
        if (employeeIds.isEmpty() && employeeRepository.count() == 0) return;

        // 고객+호텔그룹을 실제 DB에서 가져와서 호텔그룹 10개 분산 그대로 따라감
        List<Object[]> customerRows = loadCustomerHotelGroupRows();
        if (customerRows.isEmpty()) return;

        Random random = new Random();

        List<Loyalty> loyaltyBuffer = new ArrayList<>(BATCH);
        List<HistorySlot> historySlots = new ArrayList<>(BATCH);

        for (Object[] row : customerRows) {
            long customerCode = ((Number) row[0]).longValue();
            long hotelGroupCode = ((Number) row[1]).longValue();

            LocalDateTime joinedAt = randomDateTimeBetween(START, END.minusDays(10), random);
            LocalDateTime now = joinedAt;

            LoyaltyGrade grade = grades.get(random.nextInt(grades.size()));

            Loyalty loyalty = Loyalty.registerLoyalty(
                    customerCode,
                    hotelGroupCode,
                    grade.getLoyaltyGradeCode(),
                    joinedAt,
                    now
            );

            int idxInBatch = loyaltyBuffer.size();
            loyaltyBuffer.add(loyalty);

            // 이력 15%
            if (random.nextInt(100) < 15) {
                LoyaltyGrade afterGrade = grades.get(random.nextInt(grades.size()));
                LocalDateTime changedAt = joinedAt.plusDays(5 + random.nextInt(180));
                if (changedAt.isAfter(END)) changedAt = END;

                Long employeeCode = pickEmployee(employeeIds, random);

                LoyaltyHistory history = LoyaltyHistory.recordLoyaltyChange(
                        customerCode,
                        0L, // flush 후 주입
                        ChangeSource.SYSTEM,
                        employeeCode,
                        "dummy loyalty change",
                        grade.getLoyaltyGradeCode(),
                        afterGrade.getLoyaltyGradeCode(),
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

        // “해당 고객의 loyalty_code”로 정확히 매핑
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

    private Long pickEmployee(List<Long> employeeIds, Random random) {
        if (employeeIds == null || employeeIds.isEmpty()) return 1L;
        return employeeIds.get(random.nextInt(employeeIds.size()));
    }

    private List<Object[]> loadCustomerHotelGroupRows() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("select customer_code, hotel_group_code from customer").getResultList();
        return rows;
    }

    private List<Long> loadIds(String sql) {
        @SuppressWarnings("unchecked")
        List<Object> rows = em.createNativeQuery(sql).getResultList();

        List<Long> ids = new ArrayList<>(rows.size());
        for (Object o : rows) {
            if (o instanceof Number n) ids.add(n.longValue());
            else if (o instanceof String s) ids.add(Long.parseLong(s));
        }
        return ids;
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
