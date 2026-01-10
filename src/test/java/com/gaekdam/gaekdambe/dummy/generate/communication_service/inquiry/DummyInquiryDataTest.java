package com.gaekdam.gaekdambe.dummy.generate.communication_service.inquiry;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;

@Component
@Transactional
public class DummyInquiryDataTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void generate() {
        String createSql = "CREATE TABLE IF NOT EXISTS `Inquiry` ("
                + "`inquiry_code` BIGINT NOT NULL AUTO_INCREMENT,"
                + "`inquiry_status` VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS',"
                + "`inquiry_title` VARCHAR(255) NOT NULL,"
                + "`inquiry_content` TEXT NOT NULL,"
                + "`answer_content` TEXT NULL,"
                + "`created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "`updated_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "`customer_code` BIGINT NOT NULL,"
                + "`user_code` BIGINT NULL,"
                + "`inquiry_category_code` BIGINT NOT NULL,"
                + "`hotel_group_code` BIGINT NOT NULL,"
                + "`property_code` BIGINT NOT NULL,"
                + "PRIMARY KEY (`inquiry_code`)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        jdbcTemplate.execute(createSql);

        String insertSql = "INSERT INTO Inquiry (inquiry_status, inquiry_title, inquiry_content, answer_content, created_at, updated_at, customer_code, user_code, inquiry_category_code, hotel_group_code, property_code) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Random rnd = new Random(1234);
        int total = 100;
        for (int i = 1; i <= total; i++) {
            String title = String.format("테스트 문의 #%03d", i);
            String content = String.format("더미 문의 내용입니다. 번호=%03d. 설명: sample inquiry content.", i);
            String answer = null; // 아직 답변 없음
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());

            long customerCode = 1000L + rnd.nextInt(200); // 임의 고객 코드
            Long userCode = (rnd.nextInt(10) < 3) ? (100L + rnd.nextInt(20)) : null; // 일부는 담당자 있음
            long inquiryCategory = 1 + rnd.nextInt(5); // 1..5
            long hotelGroup = 1 + rnd.nextInt(5); // 1..5
            long property = 1 + rnd.nextInt(20); // 1..20

            jdbcTemplate.update(insertSql,
                    "IN_PROGRESS",
                    title,
                    content,
                    answer,
                    now,
                    now,
                    customerCode,
                    userCode,
                    inquiryCategory,
                    hotelGroup,
                    property
            );
        }
    }
}
