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
        String createSql = "CREATE TABLE IF NOT EXISTS message_journey_stage ("
                + "stage_code VARCHAR(40) NOT NULL,"
                + "stage_name VARCHAR(200) NULL,"
                + "sort_order INT NULL,"
                + "PRIMARY KEY (stage_code)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        jdbcTemplate.execute(createSql);

        String insertSql = "INSERT INTO message_journey_stage (stage_code, stage_name, sort_order) VALUES (?, ?, ?)"
                + " ON DUPLICATE KEY UPDATE stage_name = VALUES(stage_name), sort_order = VALUES(sort_order)";

        String[][] rows = new String[][]{
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
            String code = rows[i][0];
            String name = rows[i][1];
            int sortOrder = i + 1;
            jdbcTemplate.update(insertSql, code, name, sortOrder);
        }
    }
}
