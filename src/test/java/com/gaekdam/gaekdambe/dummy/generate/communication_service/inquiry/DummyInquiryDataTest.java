package com.gaekdam.gaekdambe.dummy.generate.communication_service.inquiry;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import com.gaekdam.gaekdambe.communication_service.inquiry.command.infrastructure.repository.InquiryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;

@Component
@Transactional
public class DummyInquiryDataTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private InquiryRepository inquiryRepository;

    public void generate() {
        if (inquiryRepository.count() > 0) return;

        // property_code만 사용
        List<Long> propertyCodes = jdbcTemplate.query("""
            SELECT property_code
            FROM property
        """, (rs, rowNum) -> rs.getLong(1));

        if (propertyCodes.isEmpty()) return;

        // category PK
        List<Long> categoryIds = jdbcTemplate.query("""
            SELECT inquiry_category_code
            FROM inquiry_category
            WHERE is_active = 1
        """, (rs, rowNum) -> rs.getLong(1));

        if (categoryIds.isEmpty()) return;

        // customer_code
        List<Long> customerCodes = jdbcTemplate.query("""
            SELECT customer_code
            FROM customer
        """, (rs, rowNum) -> rs.getLong(1));

        if (customerCodes.isEmpty()) return;

        // employee_code (nullable이면 비어도 OK)
        List<Long> employeeCodes = jdbcTemplate.query("""
            SELECT employee_code
            FROM employee
        """, (rs, rowNum) -> rs.getLong(1));

        // hotel_group_code 제거
        String insertSql = """
            INSERT INTO inquiry (
                inquiry_status,
                inquiry_title,
                inquiry_content,
                answer_content,
                created_at,
                updated_at,
                customer_code,
                employee_code,
                inquiry_category_code,
                property_code
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        Random rnd = new Random(1234);
        int total = 1000;

        for (int i = 1; i <= total; i++) {
            String title = String.format("테스트 문의 #%04d", i);
            String content = """
                더미 문의 내용입니다.
                - 번호: %04d
                - 설명: sample inquiry content
                """.formatted(i);

            LocalDateTime createdAt = LocalDateTime.now()
                    .minusDays(rnd.nextInt(30))
                    .minusMinutes(rnd.nextInt(24 * 60));

            Timestamp createdTs = Timestamp.valueOf(createdAt);
            Timestamp updatedTs = Timestamp.valueOf(createdAt.plusMinutes(rnd.nextInt(180)));

            Long customerCode = customerCodes.get(rnd.nextInt(customerCodes.size()));

            Long employeeCode = null;
            if (!employeeCodes.isEmpty() && rnd.nextInt(10) < 3) {
                employeeCode = employeeCodes.get(rnd.nextInt(employeeCodes.size()));
            }

            Long propertyCode = propertyCodes.get(rnd.nextInt(propertyCodes.size()));
            Long inquiryCategoryCode = categoryIds.get(rnd.nextInt(categoryIds.size()));

            boolean answered = rnd.nextInt(10) < 3;
            String status = answered ? "ANSWERED" : "IN_PROGRESS";
            String answerContent = answered ? "외부 시스템 답변 수신(더미) - 안내 완료되었습니다." : null;

            jdbcTemplate.update(
                    insertSql,
                    status,
                    title,
                    content,
                    answerContent,
                    createdTs,
                    updatedTs,
                    customerCode,
                    employeeCode,
                    inquiryCategoryCode,
                    propertyCode
            );
        }
    }
}
