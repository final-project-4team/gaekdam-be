package com.gaekdam.gaekdambe.unit.customer_service.customer.query.service;

import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.CustomerSnapshotResponse;
import com.gaekdam.gaekdambe.customer_service.customer.query.mapper.CustomerSnapshotMapper;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.CustomerSnapshotQueryService;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.row.CustomerSnapshotRow;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerSnapshotQueryServiceTest {

    private CustomerSnapshotMapper snapshotMapper;
    private CustomerSnapshotQueryService service;

    @BeforeEach
    void setUp() {
        snapshotMapper = mock(CustomerSnapshotMapper.class);
        service = new CustomerSnapshotQueryService(snapshotMapper);
    }

    @Test
    @DisplayName("getSnapshot: row 없으면 INVALID_REQUEST")
    void getSnapshot_notFound_thenThrow() {
        // given
        when(snapshotMapper.findCustomerSnapshot(1L, 100L)).thenReturn(null);

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.getSnapshot(1L, 100L),
                CustomException.class
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
        assertThat(ex.getMessage()).contains("존재하지 않는 고객");
    }

    @Test
    @DisplayName("getSnapshot: ltvAmount null이면 BigDecimal.ZERO로 내려준다")
    void getSnapshot_ltvNull_thenZero() {
        // given
        CustomerSnapshotRow row = mock(CustomerSnapshotRow.class);
        when(row.getCustomerCode()).thenReturn(100L);
        when(row.getTotalStayCount()).thenReturn(3L);
        when(row.getLtvAmount()).thenReturn(null); // 핵심 분기
        LocalDateTime lastUsedAt = LocalDateTime.now();
        when(row.getLastUsedAt()).thenReturn(lastUsedAt);
        when(row.getUnresolvedInquiryCount()).thenReturn(2L);

        when(snapshotMapper.findCustomerSnapshot(1L, 100L)).thenReturn(row);

        // when
        CustomerSnapshotResponse res = service.getSnapshot(1L, 100L);

        // then
        assertThat(res).isNotNull();

        Object ltv = read(res, "getLtvAmount", "ltvAmount");
        assertThat(ltv).isEqualTo(BigDecimal.ZERO);

        Object customerCode = read(res, "getCustomerCode", "customerCode");
        assertThat(customerCode).isEqualTo(100L);
    }

    private static Object read(Object target, String... candidates) {
        for (String name : candidates) {
            try {
                Method m = target.getClass().getMethod(name);
                return m.invoke(target);
            } catch (Exception ignore) {
            }
        }
        throw new IllegalStateException("Cannot read property methods from " + target.getClass());
    }
}
