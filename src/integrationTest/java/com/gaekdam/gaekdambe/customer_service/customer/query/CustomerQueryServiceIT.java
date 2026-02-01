package com.gaekdam.gaekdambe.customer_service.customer.query;

import com.gaekdam.gaekdambe.IntegrationTest;
import com.gaekdam.gaekdambe.IntegrationTestBase;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ContractType;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.CustomerStatus;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.NationalityType;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.CustomerStatusResponse;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.CustomerQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@Transactional
class CustomerQueryServiceIT extends IntegrationTestBase {

    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired CustomerQueryService customerQueryService;

    @DisplayName("고객 상태 조회가 DB 컨테이너(Testcontainers MariaDB)에서 정상 동작한다")
    @Test
    void getCustomerStatus_readsFromContainerDb() {
        Long hotelGroupCode = 1L;
        Long customerCode = insertCustomer(hotelGroupCode);

        CustomerStatusResponse res = customerQueryService.getCustomerStatus(hotelGroupCode, customerCode);

        assertThat(res.getCustomerCode()).isEqualTo(customerCode);
        assertThat(res.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
    }

    private Long insertCustomer(Long hotelGroupCode) {
        LocalDateTime now = LocalDateTime.now();

        NationalityType nationalityType = NationalityType.values()[0];
        ContractType contractType = ContractType.values()[0];

        String sql = """
            INSERT INTO customer (
              hotel_group_code,
              customer_name_enc,
              customer_name_hash,
              nationality_type,
              contract_type,
              customer_status,
              caution_at,
              inactive_at,
              created_at,
              updated_at,
              kms_key_id,
              dek_enc
            ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )
        """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int i = 1;

            ps.setLong(i++, hotelGroupCode);
            ps.setBytes(i++, null);
            ps.setString(i++, null);

            ps.setString(i++, nationalityType.name());
            ps.setString(i++, contractType.name());
            ps.setString(i++, CustomerStatus.ACTIVE.name());

            ps.setTimestamp(i++, null);
            ps.setTimestamp(i++, null);

            ps.setTimestamp(i++, Timestamp.valueOf(now));
            ps.setTimestamp(i++, Timestamp.valueOf(now));

            ps.setString(i++, "test-kms-key");
            ps.setBytes(i++, new byte[]{1, 2, 3}); // dek_enc (NOT NULL)

            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        assertThat(key).isNotNull();
        return key.longValue();
    }
}
