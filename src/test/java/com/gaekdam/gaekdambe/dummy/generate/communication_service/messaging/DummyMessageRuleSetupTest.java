package com.gaekdam.gaekdambe.dummy.generate.communication_service.messaging;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;

@Component
@Transactional
public class DummyMessageRuleSetupTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void generate() {


        // 테이블이 이미 존재하면 실행 안 함
        if (isTableExists("message_rule")) {
            return;
        }

        String createSql = "CREATE TABLE IF NOT EXISTS `message_rule` ("
                + "`rule_id` VARCHAR(255) NOT NULL,"
                + "`stage_code` VARCHAR(40) NOT NULL COMMENT '고객여정코드',"
                + "`template_id` BIGINT NOT NULL,"
                + "`reference_entity_type` VARCHAR(20) NOT NULL COMMENT '기준 엔티티 : RESERVATION, STAY',"
                + "`offset_minutes` INT NOT NULL DEFAULT 0 COMMENT '오프셋(분). 음수=전, 양수=후',"
                + "`visitor_type` VARCHAR(10) NULL COMMENT '방문객 타입 조건(FIRST, REPEAT)',"
                + "`membership_grade` VARCHAR(10) NULL DEFAULT 'BASIC' COMMENT '맴버십 등급 : BASIC/SILVER/GOLD/VIP 등',"
                + "`channel` VARCHAR(10) NOT NULL DEFAULT 'SMS' COMMENT '메시지채널 : SMS, EMAIL, KAKAO, PUSH',"
                + "`is_enabled` TINYINT(1) NULL COMMENT '활성 여부',"
                + "`priority` INT NOT NULL COMMENT '우선순위(낮을수록 먼저 적용)',"
                + "`description` VARCHAR(255) NULL COMMENT '룰 설명',"
                + "`created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',"
                + "`updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',"
                + "`membership_grade_code` BIGINT NOT NULL COMMENT '맴버십 등급 식별코드',"
                + "PRIMARY KEY (`rule_id`)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        jdbcTemplate.execute(createSql);

        String insertSql = "INSERT INTO message_rule (rule_id, stage_code, template_id, reference_entity_type, offset_minutes, visitor_type, membership_grade, channel, is_enabled, priority, description, created_at, updated_at, membership_grade_code) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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

        String[] referenceEntityTypes = {"RESERVATION", "STAY"};

        int idx = 0;
        for (int i = 0; i < stageCodes.length; i++) {
            String stage = stageCodes[i];
            for (String refType : referenceEntityTypes) {
                String ruleId = "rule-" + UUID.randomUUID().toString();
                long templateId = 1000L + (idx + 1); // arbitrary template id (ignore FK)
                int offset = 0;
                String visitorType = null;
                String membershipGrade = "BASIC";
                String channel = "SMS";
                int isEnabled = 1;
                int priority = idx + 1; // increasing priority
                String description = String.format("자동룰: %s / %s", stage, refType);
                Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                long membershipGradeCode = 1L;

                jdbcTemplate.update(insertSql,
                        ruleId,
                        stage,
                        templateId,
                        refType,
                        offset,
                        visitorType,
                        membershipGrade,
                        channel,
                        isEnabled,
                        priority,
                        description,
                        now,
                        now,
                        membershipGradeCode
                );

                idx++;
            }
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
