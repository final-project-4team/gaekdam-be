package com.gaekdam.gaekdambe.dummy.generate.communication_service.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;

@Component
@Transactional
public class DummyMessageJourneyStageSetupTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void generate() {

        // 테이블이 이미 있으면 실행 안 함
        if (isTableExists("message_journey_stage")) {
            return;
        }

        // 테이블 생성
        jdbcTemplate.execute("""
            CREATE TABLE message_journey_stage (
                stage_code VARCHAR(40) NOT NULL,
                stage_name VARCHAR(200),
                sort_order INT,
                PRIMARY KEY (stage_code)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """);

        String insertSql = """
            INSERT INTO message_journey_stage (stage_code, stage_name, sort_order)
            VALUES (?, ?, ?)
        """;

        String[][] rows = {
                {"RESERVATION_CONFIRMED", "예약확정"},
                {"CHECKIN_PLANNED", "체크인 예정"},
                {"CHECKIN_CONFIRMED", "체크인 확정"},
                {"CHECKOUT_PLANNED", "체크아웃 예정"},
                {"CHECKOUT_CONFIRMED", "체크아웃 확정"},
                {"RESERVATION_CANCELLED", "예약 취소 확정"},
                {"RESERVATION_UPDATED", "예약 업데이트 직후"},
                {"NOSHOW_CONFIRMED", "노쇼 직후"}
        };

        for (int i = 0; i < rows.length; i++) {
            jdbcTemplate.update(
                    insertSql,
                    rows[i][0],
                    rows[i][1],
                    i + 1
            );
        }
    }

   // 테이블 존재여부
    private boolean isTableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM information_schema.tables
            WHERE table_schema = DATABASE()
              AND table_name = ?
        """, Integer.class, tableName);

        return count != null && count > 0;
    }
}
