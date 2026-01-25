package com.gaekdam.gaekdambe.dummy.generate.customer_service.customer;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.CustomerStatus;
import com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository.CustomerRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DummyCustomerStatusPostProcess {

    private static final int CHUNK = 1000;

    @Autowired
    CustomerRepository customerRepository;

    @PersistenceContext
    EntityManager em;

    @Transactional
    public void generate() {

        if (customerRepository.count() == 0) return;

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime cautionCutoff = now.minusMonths(6);
        LocalDateTime inactiveCutoff = now.minusMonths(24);

        String sql = """
                select customer_code, max(actual_checkout_at) as last_checkout
                from stay
                where stay_status = 'COMPLETED'
                  and actual_checkout_at is not null
                group by customer_code
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(sql).getResultList();

        List<Long> inactiveIds = new ArrayList<>();
        List<Long> cautionIds = new ArrayList<>();
        List<Long> activeIds = new ArrayList<>();

        for (Object[] r : rows) {
            Long customerCode = ((Number) r[0]).longValue();
            LocalDateTime lastCheckout = toLocalDateTime(r[1]);

            if (lastCheckout.isBefore(inactiveCutoff)) inactiveIds.add(customerCode);
            else if (lastCheckout.isBefore(cautionCutoff)) cautionIds.add(customerCode);
            else activeIds.add(customerCode);
        }

        bulkUpdateInactive(inactiveIds, now);
        bulkUpdateCaution(cautionIds, now);
        bulkUpdateActive(activeIds, now);

        // stay 기록이 아예 없는 고객은 기본값 ACTIVE 그대로 둔다.
        // 만약 "stay 없는 고객은 CAUTION"으로 하고 싶으면 아래처럼 한 번 더 돌리면 된다.
        // updateNoStayCustomersToCaution(now);
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof Timestamp ts) return ts.toLocalDateTime();
        if (value instanceof LocalDateTime ldt) return ldt;
        throw new IllegalStateException("Unsupported datetime type: " + value.getClass());
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
}
