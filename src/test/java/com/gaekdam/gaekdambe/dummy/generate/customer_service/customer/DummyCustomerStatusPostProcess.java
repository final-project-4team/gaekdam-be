package com.gaekdam.gaekdambe.dummy.generate.customer_service.customer;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ChangeSource;
import com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository.CustomerRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DummyCustomerStatusPostProcess {

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

        // 1) 바뀌는 고객만 history를 DB에서 바로 insert
        // - before_status: 현재 customer.customer_status
        // - after_status : last_checkout 기준으로 계산된 target_status
        // - employee_code: 호텔그룹별 ACTIVE 직원 중 가장 작은 코드(MIN) 1명 사용 (없으면 1)
        // - changed_at   : 기존 자바 로직(6/24개월 기준)과 동일하게 계산
        String insertHistorySql = """
            INSERT INTO customer_status_history
                (customer_code, before_status, after_status, change_source, employee_code, change_reason, changed_at)
            SELECT
                c.customer_code,
                c.customer_status AS before_status,
                t.target_status   AS after_status,
                :changeSource     AS change_source,
                COALESCE(e.employee_code, 1) AS employee_code,
                'AUTO_BY_LAST_STAY' AS change_reason,
                t.changed_at      AS changed_at
            FROM customer c
            JOIN (
                SELECT
                    c2.customer_code,
                    CASE
                        WHEN s.last_checkout IS NULL THEN 'ACTIVE'
                        WHEN s.last_checkout < :inactiveCutoff THEN 'INACTIVE'
                        WHEN s.last_checkout < :cautionCutoff  THEN 'CAUTION'
                        ELSE 'ACTIVE'
                    END AS target_status,
                    CASE
                        WHEN s.last_checkout IS NULL THEN :now
                        WHEN s.last_checkout < :inactiveCutoff THEN LEAST(DATE_ADD(s.last_checkout, INTERVAL 24 MONTH), :now)
                        WHEN s.last_checkout < :cautionCutoff  THEN LEAST(DATE_ADD(s.last_checkout, INTERVAL 6 MONTH),  :now)
                        ELSE :now
                    END AS changed_at
                FROM customer c2
                LEFT JOIN (
                    SELECT customer_code, MAX(actual_checkout_at) AS last_checkout
                    FROM stay
                    WHERE stay_status = 'COMPLETED'
                      AND actual_checkout_at IS NOT NULL
                    GROUP BY customer_code
                ) s ON s.customer_code = c2.customer_code
            ) t ON t.customer_code = c.customer_code
            LEFT JOIN (
                SELECT hotel_group_code, MIN(employee_code) AS employee_code
                FROM employee
                WHERE employee_status = 'ACTIVE'
                GROUP BY hotel_group_code
            ) e ON e.hotel_group_code = c.hotel_group_code
            WHERE t.target_status <> c.customer_status
            """;

        em.createNativeQuery(insertHistorySql)
                .setParameter("now", now)
                .setParameter("cautionCutoff", cautionCutoff)
                .setParameter("inactiveCutoff", inactiveCutoff)
                .setParameter("changeSource", ChangeSource.SYSTEM.name())
                .executeUpdate();

        // 2) 바뀌는 고객만 customer 상태/날짜 필드를 DB에서 한번에 update
        // - INACTIVE: inactive_at = now, caution_at = null
        // - CAUTION : caution_at = now, inactive_at = null
        // - ACTIVE  : caution_at/inactive_at = null
        String updateCustomerSql = """
            UPDATE customer c
            LEFT JOIN (
                SELECT customer_code, MAX(actual_checkout_at) AS last_checkout
                FROM stay
                WHERE stay_status = 'COMPLETED'
                  AND actual_checkout_at IS NOT NULL
                GROUP BY customer_code
            ) s ON s.customer_code = c.customer_code
            SET
                c.customer_status = CASE
                    WHEN s.last_checkout IS NULL THEN 'ACTIVE'
                    WHEN s.last_checkout < :inactiveCutoff THEN 'INACTIVE'
                    WHEN s.last_checkout < :cautionCutoff  THEN 'CAUTION'
                    ELSE 'ACTIVE'
                END,
                c.caution_at = CASE
                    WHEN s.last_checkout IS NOT NULL
                     AND s.last_checkout >= :inactiveCutoff
                     AND s.last_checkout <  :cautionCutoff
                    THEN :now
                    ELSE NULL
                END,
                c.inactive_at = CASE
                    WHEN s.last_checkout IS NOT NULL
                     AND s.last_checkout < :inactiveCutoff
                    THEN :now
                    ELSE NULL
                END,
                c.updated_at = :now
            WHERE
                c.customer_status <> CASE
                    WHEN s.last_checkout IS NULL THEN 'ACTIVE'
                    WHEN s.last_checkout < :inactiveCutoff THEN 'INACTIVE'
                    WHEN s.last_checkout < :cautionCutoff  THEN 'CAUTION'
                    ELSE 'ACTIVE'
                END
            """;

        em.createNativeQuery(updateCustomerSql)
                .setParameter("now", now)
                .setParameter("cautionCutoff", cautionCutoff)
                .setParameter("inactiveCutoff", inactiveCutoff)
                .executeUpdate();

        em.flush();
        em.clear();
    }
}
