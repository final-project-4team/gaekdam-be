package com.gaekdam.gaekdambe.dummy.generate.customer_service.customer;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ChangeSource;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.CustomerStatus;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.CustomerStatusHistory;
import com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository.CustomerRepository;
import com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository.CustomerStatusHistoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class DummyCustomerStatusPostProcess {

    private static final int CHUNK = 1000;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    CustomerStatusHistoryRepository customerStatusHistoryRepository;

    @PersistenceContext
    EntityManager em;

    @Transactional
    public void generate() {

        if (customerRepository.count() == 0) return;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cautionCutoff = now.minusMonths(6);
        LocalDateTime inactiveCutoff = now.minusMonths(24);

        // hotel_group_code -> employee_code 목록 (ACTIVE 직원만)
        Map<Long, List<Long>> employeeIdsByHotelGroup = loadEmployeeIdsByHotelGroup();
        List<Long> fallbackEmployees = employeeIdsByHotelGroup.values().stream()
                .flatMap(List::stream)
                .distinct()
                .toList();

        // customer + 마지막 completed checkout을 한 번에 가져옴 (stay 없는 고객도 포함)
        // last_checkout == null 이면 stay 없는 고객
        String sql = """
                select
                    c.customer_code,
                    c.hotel_group_code,
                    c.customer_status,
                    max(s.actual_checkout_at) as last_checkout
                from customer c
                left join stay s
                       on s.customer_code = c.customer_code
                      and s.stay_status = 'COMPLETED'
                      and s.actual_checkout_at is not null
                group by c.customer_code, c.hotel_group_code, c.customer_status
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(sql).getResultList();
        if (rows.isEmpty()) return;

        List<Long> idsInactive = new ArrayList<>();
        List<Long> idsCaution = new ArrayList<>();
        List<Long> idsActive  = new ArrayList<>();

        // history는 “바뀐 건만” 저장
        List<CustomerStatusHistory> historyBuffer = new ArrayList<>(CHUNK);

        // customer별로 “바뀌어야 하는 상태” 계산
        // - last_checkout < now-24m => INACTIVE
        // - last_checkout < now-6m  => CAUTION
        // - else ACTIVE
        // - last_checkout null(투숙 없음) => ACTIVE 유지 (원하면 CAUTION으로 변경 가능)
        for (Object[] r : rows) {
            Long customerCode = ((Number) r[0]).longValue();
            Long hotelGroupCode = ((Number) r[1]).longValue();
            CustomerStatus currentStatus = CustomerStatus.valueOf(String.valueOf(r[2]));
            LocalDateTime lastCheckout = toLocalDateTime(r[3]);

            CustomerStatus targetStatus;
            LocalDateTime changedAt;

            if (lastCheckout == null) {
                // stay 없는 고객 정책
                targetStatus = CustomerStatus.ACTIVE;     // ✅ 현재 정책: ACTIVE 유지
                // targetStatus = CustomerStatus.CAUTION;  // (옵션) stay 없는 고객을 CAUTION으로
                changedAt = now;
            } else if (lastCheckout.isBefore(inactiveCutoff)) {
                targetStatus = CustomerStatus.INACTIVE;
                // “24개월 기준으로 바뀌었다”는 의미로, 이론상 변화시점 = lastCheckout + 24개월
                changedAt = lastCheckout.plusMonths(24);
                if (changedAt.isAfter(now)) changedAt = now;
            } else if (lastCheckout.isBefore(cautionCutoff)) {
                targetStatus = CustomerStatus.CAUTION;
                changedAt = lastCheckout.plusMonths(6);
                if (changedAt.isAfter(now)) changedAt = now;
            } else {
                targetStatus = CustomerStatus.ACTIVE;
                changedAt = now;
            }

            // 바뀌는 경우만 update + history
            if (targetStatus != currentStatus) {

                // 업데이트 대상 모으기(상태별)
                if (targetStatus == CustomerStatus.INACTIVE) idsInactive.add(customerCode);
                else if (targetStatus == CustomerStatus.CAUTION) idsCaution.add(customerCode);
                else idsActive.add(customerCode);

                Long employeeCode = pickEmployee(hotelGroupCode, employeeIdsByHotelGroup, fallbackEmployees);

                historyBuffer.add(
                        CustomerStatusHistory.recordCustomerStatusChange(
                                customerCode,
                                currentStatus,
                                targetStatus,
                                ChangeSource.SYSTEM,
                                employeeCode,
                                "AUTO_BY_LAST_STAY",
                                changedAt
                        )
                );

                // historyBuffer는 CHUNK 단위로 저장
                if (historyBuffer.size() == CHUNK) {
                    customerStatusHistoryRepository.saveAll(historyBuffer);
                    em.flush();
                    em.clear();
                    historyBuffer.clear();
                }
            }
        }

        // 상태 업데이트(상태별로 chunk update)
        bulkUpdateInactive(idsInactive, now);
        bulkUpdateCaution(idsCaution, now);
        bulkUpdateActive(idsActive, now);

        // 남은 history 저장
        if (!historyBuffer.isEmpty()) {
            customerStatusHistoryRepository.saveAll(historyBuffer);
            em.flush();
            em.clear();
        }
    }

    private void bulkUpdateInactive(List<Long> ids, LocalDateTime now) {
        if (ids == null || ids.isEmpty()) return;
        for (int i = 0; i < ids.size(); i += CHUNK) {
            List<Long> chunk = ids.subList(i, Math.min(i + CHUNK, ids.size()));
            em.createQuery("""
                    update Customer c
                       set c.customerStatus = :status,
                           c.inactiveAt = :now,
                           c.updatedAt = :now
                     where c.customerCode in :ids
                    """)
                    .setParameter("status", CustomerStatus.INACTIVE)
                    .setParameter("now", now)
                    .setParameter("ids", chunk)
                    .executeUpdate();
            em.flush();
            em.clear();
        }
    }

    private void bulkUpdateCaution(List<Long> ids, LocalDateTime now) {
        if (ids == null || ids.isEmpty()) return;
        for (int i = 0; i < ids.size(); i += CHUNK) {
            List<Long> chunk = ids.subList(i, Math.min(i + CHUNK, ids.size()));
            em.createQuery("""
                    update Customer c
                       set c.customerStatus = :status,
                           c.cautionAt = :now,
                           c.updatedAt = :now
                     where c.customerCode in :ids
                    """)
                    .setParameter("status", CustomerStatus.CAUTION)
                    .setParameter("now", now)
                    .setParameter("ids", chunk)
                    .executeUpdate();
            em.flush();
            em.clear();
        }
    }

    private void bulkUpdateActive(List<Long> ids, LocalDateTime now) {
        if (ids == null || ids.isEmpty()) return;
        for (int i = 0; i < ids.size(); i += CHUNK) {
            List<Long> chunk = ids.subList(i, Math.min(i + CHUNK, ids.size()));
            em.createQuery("""
                    update Customer c
                       set c.customerStatus = :status,
                           c.cautionAt = null,
                           c.inactiveAt = null,
                           c.updatedAt = :now
                     where c.customerCode in :ids
                    """)
                    .setParameter("status", CustomerStatus.ACTIVE)
                    .setParameter("now", now)
                    .setParameter("ids", chunk)
                    .executeUpdate();
            em.flush();
            em.clear();
        }
    }

    private Map<Long, List<Long>> loadEmployeeIdsByHotelGroup() {
        // employee_status 컬럼이 문자열 enum('ACTIVE')라고 가정
        String sql = """
                select hotel_group_code, employee_code
                from employee
                where employee_status = 'ACTIVE'
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(sql).getResultList();

        Map<Long, List<Long>> map = new HashMap<>();
        for (Object[] r : rows) {
            Long hotelGroupCode = ((Number) r[0]).longValue();
            Long employeeCode = ((Number) r[1]).longValue();
            map.computeIfAbsent(hotelGroupCode, k -> new ArrayList<>()).add(employeeCode);
        }
        map.values().forEach(list -> list.sort(Comparator.naturalOrder()));
        return map;
    }

    private Long pickEmployee(Long hotelGroupCode,
                              Map<Long, List<Long>> map,
                              List<Long> fallback) {
        List<Long> list = map.get(hotelGroupCode);
        if (list != null && !list.isEmpty()) return list.get(0); // 대표 1명 고정(원하면 랜덤으로 바꿔도 됨)
        if (fallback != null && !fallback.isEmpty()) return fallback.get(0);
        return 1L; // 최후 fallback
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof Timestamp ts) return ts.toLocalDateTime();
        if (value instanceof LocalDateTime ldt) return ldt;
        throw new IllegalStateException("Unsupported datetime type: " + value.getClass());
    }
}
