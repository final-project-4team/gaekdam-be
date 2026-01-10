package com.gaekdam.gaekdambe.dummy.generate.communication_service.messaging;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;

@Component
@Transactional
public class DummyMessageTemplateSetupTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void generate() {
        String createSql = "CREATE TABLE IF NOT EXISTS `message_template` ("
                + "`template_id` BIGINT NOT NULL,"
                + "`stage_code` VARCHAR(40) NOT NULL COMMENT '고객여정코드',"
                + "`visitor_type` VARCHAR(10) NOT NULL COMMENT 'FIRST, REPEAT',"
                + "`language_code` VARCHAR(10) NULL DEFAULT 'KOR' COMMENT 'KOR, ENG, JPN',"
                + "`membership_grade` VARCHAR(10) NULL DEFAULT 'BASIC' COMMENT '맴버십 등급 : BASIC/SILVER/GOLD/VIP 등',"
                + "`title` VARCHAR(200) NULL COMMENT '메세지 제목',"
                + "`content` TEXT NULL COMMENT '메세지 본문',"
                + "`condition_expr` VARCHAR(500) NULL COMMENT '추가 조건(선택, 간단표현식)',"
                + "`is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '활성여부',"
                + "`created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',"
                + "`updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',"
                + "`membership_grade_code` BIGINT NOT NULL COMMENT '맴버십 등급 식별코드',"
                + "PRIMARY KEY (`template_id`)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        jdbcTemplate.execute(createSql);

        String insertSql = "INSERT INTO message_template (template_id, stage_code, visitor_type, language_code, membership_grade, title, content, condition_expr, is_active, created_at, updated_at, membership_grade_code) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String[] stageCodes = {
                "RESERVATION_CONFIRMED",
                "CHECKIN_PLANNED",
                "CHECKIN_CONFIRMED",
                "CHECKOUT_PLANNED",
                "CHECKOUT_CONFIRMED",
                "RESERVATION_CANCELLED",
                "RESERVATION_UPDATED",
                "NOSHOW_CONFIRMED"
        };

        String[] stageNames = {
                "예약 확정",
                "체크인 예정",
                "체크인 확정",
                "체크아웃 예정",
                "체크아웃 확정",
                "예약 취소",
                "예약 업데이트",
                "노쇼 알림"
        };

        String[] visitors = {"FIRST", "REPEAT"};

        // Avoid primary key collisions: if table exists, get current max(template_id), otherwise start at 1000
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND LOWER(table_name) = LOWER('message_template')",
                Integer.class);
        Long currentMax = 0L;
        if (exists != null && exists > 0) {
            currentMax = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(template_id), 0) FROM `message_template`", Long.class);
        }
        long templateBase = Math.max(1000L, currentMax != null ? currentMax : 0L);
        int idx = 0;

        for (int i = 0; i < stageCodes.length; i++) {
            String code = stageCodes[i];
            String name = stageNames[i];
            for (String visitor : visitors) {
                long templateId = templateBase + (++idx);

                String visitorLabel = visitor.equals("FIRST") ? "첫 방문 고객님" : "재방문 고객님";
                String title = String.format("%s - %s님을 위한 안내", name, visitor.equals("FIRST") ? "고객" : "재방문");
                String content = String.format("안녕하세요. %s. %s님께 알맞은 안내를 드립니다. (상태: %s, 대상: %s)", name, visitorLabel, code, visitor);
                String condition = null;

                long membershipGradeCode = visitor.equals("FIRST") ? 1L : 2L;
                String membershipGrade = visitor.equals("FIRST") ? "BASIC" : "SILVER";

                jdbcTemplate.update(insertSql,
                        templateId,
                        code,
                        visitor,
                        "KOR",
                        membershipGrade,
                        title,
                        content,
                        condition,
                        1,
                        Timestamp.valueOf(LocalDateTime.now()),
                        Timestamp.valueOf(LocalDateTime.now()),
                        membershipGradeCode
                );
            }
        }
    }
}
