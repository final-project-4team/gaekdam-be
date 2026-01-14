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

        //  property_code + hotel_group_code 세트로 가져오기 (조인/조회 안전)
        List<PropertyPair> props = jdbcTemplate.query("""
            SELECT property_code, hotel_group_code
            FROM property
        """, (rs, rowNum) -> new PropertyPair(rs.getLong(1), rs.getLong(2)));

        if (props.isEmpty()) return;

        //  실제 category PK 가져오기 (FK 안전)
        List<Long> categoryIds = jdbcTemplate.query("""
            SELECT inquiry_category_code
            FROM inquiry_category
            WHERE is_active = 1
        """, (rs, rowNum) -> rs.getLong(1));

        if (categoryIds.isEmpty()) return;

        String insertSql = """
            INSERT INTO inquiry (
                inquiry_status,
                inquiry_title,
                inquiry_content,
                answer_content,
                created_at,
                updated_at,
                customer_code,
                user_code,
                inquiry_category_code,
                hotel_group_code,
                property_code
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        Random rnd = new Random(1234);
        int total = 100;

        for (int i = 1; i <= total; i++) {
            String title = String.format("테스트 문의 #%03d", i);
            String content = String.format("더미 문의 내용입니다. 번호=%03d. 설명: sample inquiry content.", i);

            Timestamp now = Timestamp.valueOf(LocalDateTime.now());

            long customerCode = 1000L + rnd.nextInt(200);
            Long userCode = (rnd.nextInt(10) < 3) ? (100L + rnd.nextInt(20)) : null;

            //  property -> hotelGroup 같이 매칭
            PropertyPair pickedProp = props.get(rnd.nextInt(props.size()));
            long propertyCode = pickedProp.propertyCode();
            long hotelGroupCode = pickedProp.hotelGroupCode();

            //  category PK 실존값
            long inquiryCategoryCode = categoryIds.get(rnd.nextInt(categoryIds.size()));

            jdbcTemplate.update(
                    insertSql,
                    "IN_PROGRESS",
                    title,
                    content,
                    null,
                    now,
                    now,
                    customerCode,
                    userCode,
                    inquiryCategoryCode,
                    hotelGroupCode,
                    propertyCode
            );
        }
    }

    private record PropertyPair(long propertyCode, long hotelGroupCode) {}
}
