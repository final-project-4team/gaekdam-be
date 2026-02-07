package com.gaekdam.gaekdambe.dummy.generate.customer_service.membership;

import com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository.MembershipGradeRepository;
import com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository.MembershipRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
public class DummyMembershipDataTest {

    private static final LocalDateTime START = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime END   = LocalDateTime.of(2026, 12, 31, 23, 59);

    @Autowired MembershipRepository membershipRepository;
    @Autowired MembershipGradeRepository membershipGradeRepository;

    @PersistenceContext EntityManager em;

    @Transactional
    public void generate() {

        // 더미 재실행 필요하면 membership / membership_history truncate 하거나, 아래 count 스킵 제거
        if (membershipRepository.count() > 0) return;

        // 작년(캘린더) 기준 산정: END=2026 -> 2025-01-01 ~ 2025-12-31
        int targetYear = END.getYear() - 1;
        LocalDateTime statsStart = LocalDateTime.of(targetYear, 1, 1, 0, 0, 0);
        LocalDateTime statsEnd   = LocalDateTime.of(targetYear, 12, 31, 23, 59, 59);

        /**
         * 1) customer 전체에 BASIC 멤버십 먼저 생성 (stay 없어도 생성됨 → 호텔그룹 4~10 해결)
         * - joined_at은 START ~ (END-30일) 랜덤
         * - expired_at은 joined_at의 연도 12/31
         */
        String insertBasicForAllCustomers = """
            INSERT INTO membership (
                customer_code,
                hotel_group_code,
                membership_grade_code,
                membership_status,
                joined_at,
                expired_at,
                calculated_at,
                created_at,
                updated_at
            )
            SELECT
                c.customer_code,
                c.hotel_group_code,
                mg.membership_grade_code,
                'ACTIVE',
                t.joined_at,
                TIMESTAMP(CONCAT(YEAR(t.joined_at), '-12-31 23:59:59')) AS expired_at,
                NOW() AS calculated_at,
                NOW(),
                NOW()
            FROM customer c
            JOIN membership_grade mg
              ON mg.hotel_group_code = c.hotel_group_code
             AND mg.grade_name = 'BASIC'
            JOIN (
                SELECT
                    c2.customer_code,
                    TIMESTAMPADD(
                        SECOND,
                        FLOOR(RAND() * TIMESTAMPDIFF(SECOND, :start, :endMinus30)),
                        :start
                    ) AS joined_at
                FROM customer c2
            ) t ON t.customer_code = c.customer_code
            """;

        em.createNativeQuery(insertBasicForAllCustomers)
                .setParameter("start", Timestamp.valueOf(START))
                .setParameter("endMinus30", Timestamp.valueOf(END.minusDays(30)))
                .executeUpdate();

        em.flush();

        /**
         * 2) COMPLETED stay 있는 고객만 “작년(2025) 사용금액 기준 최고 등급”으로 membership_grade_code 업데이트
         * - fan-out 방지(예약/시설 따로 고객별 집계 후 더함)
         */
        String upgradeByAmountSql = """
            UPDATE membership m
            JOIN (
                SELECT
                    picked.customer_code,
                    picked.hotel_group_code,
                    picked.membership_grade_code
                FROM (
                    SELECT
                        x.customer_code,
                        x.hotel_group_code,
                        x.membership_grade_code,
                        ROW_NUMBER() OVER (
                            PARTITION BY x.customer_code, x.hotel_group_code
                            ORDER BY x.tier_level DESC
                        ) AS rn
                    FROM (
                        SELECT
                            amt.customer_code,
                            amt.hotel_group_code,
                            mg.membership_grade_code,
                            mg.tier_level
                        FROM (
                            SELECT
                                base.customer_code,
                                base.hotel_group_code,
                                COALESCE(resv.resv_amount, 0) + COALESCE(fac.fac_amount, 0) AS total_amount
                            FROM (
                                SELECT DISTINCT
                                    s.customer_code,
                                    c.hotel_group_code
                                FROM stay s
                                JOIN customer c ON c.customer_code = s.customer_code
                                WHERE s.stay_status = 'COMPLETED'
                                  AND s.actual_checkout_at BETWEEN ?1 AND ?2
                            ) base
                            LEFT JOIN (
                                SELECT
                                    s.customer_code,
                                    SUM(r.total_price) AS resv_amount
                                FROM stay s
                                JOIN reservation r ON r.reservation_code = s.reservation_code
                                WHERE s.stay_status = 'COMPLETED'
                                  AND s.actual_checkout_at BETWEEN ?1 AND ?2
                                GROUP BY s.customer_code
                            ) resv ON resv.customer_code = base.customer_code
                            LEFT JOIN (
                                SELECT
                                    s.customer_code,
                                    SUM(fu.usage_price) AS fac_amount
                                FROM stay s
                                JOIN facility_usage fu ON fu.stay_code = s.stay_code
                                WHERE s.stay_status = 'COMPLETED'
                                  AND s.actual_checkout_at BETWEEN ?1 AND ?2
                                GROUP BY s.customer_code
                            ) fac ON fac.customer_code = base.customer_code
                        ) amt
                        JOIN membership_grade mg
                          ON mg.hotel_group_code = amt.hotel_group_code
                        WHERE mg.calculation_amount IS NULL
                           OR amt.total_amount >= mg.calculation_amount
                    ) x
                ) picked
                WHERE picked.rn = 1
            ) best
              ON best.customer_code = m.customer_code
             AND best.hotel_group_code = m.hotel_group_code
            SET
              m.membership_grade_code = best.membership_grade_code,
              m.updated_at = NOW(),
              m.calculated_at = NOW()
            """;

        em.createNativeQuery(upgradeByAmountSql)
                .setParameter(1, Timestamp.valueOf(statsStart))
                .setParameter(2, Timestamp.valueOf(statsEnd))
                .executeUpdate();

        em.flush();

        /**
         * 3) membership_history 생성
         * - BASIC로 생성된 고객도 전부 히스토리 1건 들어가게 됨
         */
        String insertHistorySql = """
            INSERT INTO membership_history (
                customer_code,
                membership_code,
                change_source,
                employee_code,
                change_reason,
                before_grade,
                after_grade,
                before_status,
                after_status,
                before_expires_at,
                after_expires_at,
                changed_at,
                membership_grade_code
            )
            SELECT
                m.customer_code,
                m.membership_code,
                'SYSTEM',
                COALESCE(e.employee_code, 1),
                'Initial Membership (Dummy)',
                NULL,
                mg.grade_name,
                'ACTIVE',
                'ACTIVE',
                NULL,
                m.expired_at,
                m.joined_at,
                m.membership_grade_code
            FROM membership m
            JOIN membership_grade mg ON m.membership_grade_code = mg.membership_grade_code
            LEFT JOIN (
                SELECT hotel_group_code, MIN(employee_code) AS employee_code
                FROM employee
                WHERE employee_status = 'ACTIVE'
                GROUP BY hotel_group_code
            ) e ON m.hotel_group_code = e.hotel_group_code
            WHERE NOT EXISTS (
                SELECT 1 FROM membership_history mh WHERE mh.membership_code = m.membership_code
            )
            """;

        em.createNativeQuery(insertHistorySql).executeUpdate();

        em.flush();
        em.clear();
    }
}
