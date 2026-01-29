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

    // ✅ 승급 비율: 15%면 대략 15%가 EXCELLENT로 저장됨
    private static final int EXCELLENT_PROMOTION_RATE = 15;

    private static final LocalDateTime START = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 12, 31, 23, 59);

    @Autowired LoyaltyRepository loyaltyRepository;
    @Autowired LoyaltyHistoryRepository loyaltyHistoryRepository;

    @PersistenceContext EntityManager em;

    @Transactional
    public void generate() {

        // ✅ 이미 loyalty 있으면 스킵(다시 만들고 싶으면 loyalty/loyalty_history 비우고 실행)
        if (loyaltyRepository.count() > 0) return;

        // ✅ 호텔그룹별 GENERAL/EXCELLENT 등급코드 매핑
        Map<Long, GradePair> gradeByHotelGroup = loadGradePairsByHotelGroup();
        if (gradeByHotelGroup.isEmpty()) return;

        List<Object[]> customerRows = loadCustomerHotelGroupRows();
        if (customerRows.isEmpty()) return;

        Map<Long, List<Long>> employeeByHotelGroup = loadActiveEmployeeCodesByHotelGroup();
        List<Long> fallbackEmployees = new ArrayList<>();
        for (List<Long> list : employeeByHotelGroup.values()) {
            fallbackEmployees.addAll(list);
        }
        fallbackEmployees = fallbackEmployees.stream().distinct().toList();
        if (fallbackEmployees.isEmpty()) return;

        Random random = new Random();

        List<Loyalty> loyaltyBuffer = new ArrayList<>(BATCH);
        List<HistorySlot> historySlots = new ArrayList<>(BATCH);

        for (Object[] row : customerRows) {
            long customerCode = ((Number) row[0]).longValue();
            long hotelGroupCode = ((Number) row[1]).longValue();

            GradePair pair = gradeByHotelGroup.get(hotelGroupCode);
            if (pair == null || pair.generalCode == null) continue;

            LocalDateTime joinedAt = randomDateTimeBetween(START, END.minusDays(10), random);
            LocalDateTime now = joinedAt;

            // ✅ 기본은 GENERAL
            Loyalty loyalty = Loyalty.registerLoyalty(
                    customerCode,
                    hotelGroupCode,
                    pair.generalCode,
                    joinedAt,
                    now
            );

            boolean canPromote = pair.excellentCode != null && !Objects.equals(pair.generalCode, pair.excellentCode);
            boolean promote = canPromote && random.nextInt(100) < EXCELLENT_PROMOTION_RATE;

            int idxInBatch = loyaltyBuffer.size();
            loyaltyBuffer.add(loyalty);

            // ✅ 승급 대상이면: loyalty 테이블도 EXCELLENT로 저장 + history도 남김
            if (promote) {
                setLoyaltyGradeCode(loyalty, pair.excellentCode);

                LocalDateTime changedAt = joinedAt.plusDays(5 + random.nextInt(180));
                if (changedAt.isAfter(END)) changedAt = END;

                Long employeeCode = pickEmployeeCode(hotelGroupCode, employeeByHotelGroup, fallbackEmployees, random);

                LoyaltyHistory history = LoyaltyHistory.recordLoyaltyChange(
                        customerCode,
                        0L, // flush 후 주입
                        ChangeSource.SYSTEM,
                        employeeCode,
                        "dummy loyalty promotion",
                        pair.generalCode,
                        pair.excellentCode,
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
    private record GradePair(Long generalCode, Long excellentCode) {}

    /**
     * ✅ @SuppressWarnings 없이 안전하게 처리
     * - List<?>로 받고, 각 row를 Object[]로 캐스팅해서 사용
     */
    private Map<Long, GradePair> loadGradePairsByHotelGroup() {

        List<?> raw = em.createNativeQuery(
                "select hotel_group_code, loyalty_grade_name, loyalty_grade_code " +
                        "  from loyalty_grade " +
                        " where loyalty_grade_status = 'ACTIVE'"
        ).getResultList();

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

    /**
     * ✅ (중요) 네가 올린 코드에서 여기 return 누락되어 있었음 → 컴파일 에러/런타임 문제 원인
     */
    private List<Object[]> loadCustomerHotelGroupRows() {

        List<?> raw = em.createNativeQuery(
                "select customer_code, hotel_group_code " +
                        "  from customer"
        ).getResultList();

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
                        "   and hotel_group_code is not null"
        ).getResultList();

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

    // Loyalty 엔티티에 setter가 없어서 reflection으로 강제 주입
    private static void setLoyaltyGradeCode(Loyalty loyalty, long loyaltyGradeCode) {
        List<String> candidates = Arrays.asList(
                "loyaltyGradeCode",
                "loyalty_grade_code",
                "loyaltyGrade"
        );

        for (String fieldName : candidates) {
            try {
                var f = Loyalty.class.getDeclaredField(fieldName);
                f.setAccessible(true);
                f.set(loyalty, loyaltyGradeCode);
                return;
            } catch (NoSuchFieldException ignore) {
                // next candidate
            } catch (Exception e) {
                throw new RuntimeException("Loyalty." + fieldName + " set failed", e);
            }
        }
        throw new RuntimeException("Loyalty grade field not found. check Loyalty entity field name.");
    }
}
