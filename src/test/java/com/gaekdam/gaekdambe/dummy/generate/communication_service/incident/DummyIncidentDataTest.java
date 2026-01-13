package com.gaekdam.gaekdambe.dummy.generate.communication_service.incident;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Random;

import com.gaekdam.gaekdambe.communication_service.incident.command.domain.infrastructure.repository.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;

/**
 * Incident 더미데이터 생성기
 * - 테이블이 없으면 CREATE TABLE 실행
 * - 100건의 더미 Incident 레코드 삽입
 */
@Component
@Transactional
public class DummyIncidentDataTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private IncidentRepository incidentRepository;

    public void generate() {

        // 테이블이 이미 존재하면 실행 안 함
        if (isTableExists("Incident")) {
            return;
        }

        String createSql = "CREATE TABLE IF NOT EXISTS `Incident` ("
                + "`incident_code` BIGINT NOT NULL AUTO_INCREMENT COMMENT '사건 사고 보고서 식별코드',"
                + "`inquiry_code` BIGINT NULL COMMENT '필요시 문의 참조에 사용',"
                + "`employee_code` BIGINT NOT NULL COMMENT '담당자',"
                + "`incident_title` VARCHAR(200) NOT NULL COMMENT '보고서 제목',"
                + "`incident_summary` VARCHAR(500) NULL COMMENT '사건 요약',"
                + "`incident_content` MEDIUMTEXT NOT NULL COMMENT '사건사고 보고서 상세내용',"
                + "`severity` VARCHAR(10) NULL DEFAULT 'MEDIUM' COMMENT 'LOW/MEDIUM/HIGH/CRITICAL',"
                + "`incident_type` VARCHAR(50) NOT NULL COMMENT '시설/결재/고객/직원/기타',"
                + "`incident_status` VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS' COMMENT '조치중 / 종결',"
                + "`occured_at` DATETIME NULL COMMENT '사건 사고 발생시각',"
                + "`created_at` DATETIME NOT NULL COMMENT '작성일',"
                + "`updated_at` DATETIME NOT NULL COMMENT '수정일',"
                + "`hotel_group_code` BIGINT NOT NULL COMMENT '호텔 그룹 식별 코드',"
                + "`property_code` BIGINT NOT NULL COMMENT '지점 식별 코드',"
                + "PRIMARY KEY (`incident_code`)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        jdbcTemplate.execute(createSql);

        String insertSql = "INSERT INTO Incident (inquiry_code, employee_code, incident_title, incident_summary, incident_content, severity, incident_type, incident_status, occured_at, created_at, updated_at, hotel_group_code, property_code) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Random rnd = new Random(20260110);
        String[] severities = {"LOW", "MEDIUM", "HIGH", "CRITICAL"};
        String[] types = {"FACILITY", "PAYMENT", "CUSTOMER", "STAFF", "OTHER"};
        String[] statuses = {"IN_PROGRESS", "RESOLVED", "CLOSED"};

        int total = 100;
        for (int i = 1; i <= total; i++) {
            Long inquiryCode = (rnd.nextInt(10) < 2) ? (long) (1000 + rnd.nextInt(200)) : null; // 일부는 문의 참조
            long employeeCode = 100L + rnd.nextInt(50);
            String title = String.format("사건 보고 #%03d - %s", i, types[rnd.nextInt(types.length)]);
            String summary = String.format("간단 요약입니다. 사건번호=%03d", i);
            String content = String.format("상세 보고 내용 (샘플) - 사건번호=%03d. 발생상세: 자세한 내용 작성 필요.", i);
            String severity = severities[rnd.nextInt(severities.length)];
            String type = types[rnd.nextInt(types.length)];
            String status = statuses[rnd.nextInt(statuses.length)];

            LocalDateTime occured = LocalDateTime.now().minusDays(rnd.nextInt(60)).minusHours(rnd.nextInt(24));
            Timestamp occuredAt = Timestamp.valueOf(occured);
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());

            long hotelGroup = 1 + rnd.nextInt(5);
            long property = 1 + rnd.nextInt(20);

            jdbcTemplate.update(insertSql,
                    inquiryCode,
                    employeeCode,
                    title,
                    summary,
                    content,
                    severity,
                    type,
                    status,
                    occuredAt,
                    now,
                    now,
                    hotelGroup,
                    property
            );
        }
    }


    /**
     * 테이블 존재 여부 확인
     */
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
