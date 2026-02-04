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
            Loyalty loyalty = Loyalty.registerLoyalty(
                    customerCode,
                    hotelGroupCode,
                    pair.generalCode,
                    joinedAt,
                    now);

            // 가입 시점 세팅
            // 산정일 = 가입일
            // 로열티는 만료 개념이 명시적이지 않다면 calculatedAt 기준 1년 뒤 갱신으로 가정 or 로직에 따름
            // 여기서는 가입 시점 상태 저장

            int idxInBatch = loyaltyBuffer.size();
            loyaltyBuffer.add(loyalty);

            // 2. 연도 갱신 시뮬레이션 (매년 1월 1일 등급 재산정 가정)
            int currentYear = joinedAt.getYear();
            int maxYear = END.getYear(); // 2026

            // 현재 등급 (가입 시점)
            Long currentGradeCode = pair.generalCode;

            // 반복적으로 해를 넘기며 갱신
            while (currentYear < maxYear && random.nextBoolean()) { // 50% 확률로 다음 해 유지
                int nextYear = currentYear + 1;
                LocalDateTime renewalDate = LocalDateTime.of(nextYear, 1, 1, 7, 38); // 배치 실행 시간

                boolean canPromote = pair.excellentCode != null
                        && !Objects.equals(pair.generalCode, pair.excellentCode);

                // 확률적으로 등급 변경 (EXCELLENT 승급 or 유지)
                // 이미 EXCELLENT면 유지, GENERAL이면 승급 시도
                Long nextGradeCode = currentGradeCode;
                boolean isExcellent = Objects.equals(currentGradeCode, pair.excellentCode);

                if (canPromote) {
                    if (!isExcellent && random.nextInt(100) < EXCELLENT_PROMOTION_RATE) {
                        nextGradeCode = pair.excellentCode; // 승급
                    } else if (isExcellent && random.nextInt(100) < 5) {
                        nextGradeCode = pair.generalCode; // 강등 (드물게)
                    }
                }

                if (!Objects.equals(currentGradeCode, nextGradeCode)) {
                    // 등급 변경 발생 시 이력 저장
                    // 생략하거나
                    // map
                    // 활용
                    // 필요
                    // 간소화를 위해 gradeCode만 기록하거나, 위에서 map에 이름을 같이 담아뒀어야 함.
                    // (GradePair에 이름 추가 필요하지만, 일단 로직 흐름 구현)

                    LoyaltyHistory history = LoyaltyHistory.recordLoyaltyChange(
                            customerCode,
                            0L, // flush 후 주입
                            ChangeSource.SYSTEM,
                            null, // system
                            "Automatic Loyalty Grade Update (Dummy Simulation)",
                            currentGradeCode,
                            nextGradeCode,
                            LoyaltyStatus.ACTIVE,
                            LoyaltyStatus.ACTIVE,
                            renewalDate);
                    historySlots.add(new HistorySlot(idxInBatch, history));

                    // 현재 등급 상태 업데이트 (메모리 반영 -> 마지막 상태가 DB 저장됨)
                    // setter가 없어서 리플렉션 써야 함 (setLoyaltyGradeCode 재사용)
                    setLoyaltyGradeCode(loyalty, nextGradeCode);

                    // calculatedAt 업데이트 필요 (엔티티에 setter 없음, changeLoyaltyGrade 사용해야 함 which handles
                    // both)
                    // loyalty.changeLoyaltyGrade(nextGradeCode, renewalDate); // 접근 제어자가 public이면
                    // 이것 사용
                    // Loyalty.java 보니 changeLoyaltyGrade가 public임. 리플렉션 대신 이거 사용 권장.
                    loyalty.changeLoyaltyGrade(nextGradeCode, renewalDate);

                    currentGradeCode = nextGradeCode;
                }

                currentYear = nextYear;
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

    // Loyalty 엔티티에 setter가 없어서 reflection으로 강제 주입
    private static void setLoyaltyGradeCode(Loyalty loyalty, long loyaltyGradeCode) {
        List<String> candidates = Arrays.asList(
                "loyaltyGradeCode",
                "loyalty_grade_code",
                "loyaltyGrade");

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
