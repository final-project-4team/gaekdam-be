package com.gaekdam.gaekdambe.unit.customer_service.customer.query.service;

import com.gaekdam.gaekdambe.customer_service.customer.query.dto.request.CustomerMemoSearchRequest;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.CustomerMemoResponse;
import com.gaekdam.gaekdambe.customer_service.customer.query.mapper.CustomerMemoMapper;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.CustomerMemoQueryService;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerMemoQueryServiceTest {

    private CustomerMemoMapper customerMemoMapper;
    private CustomerMemoQueryService service;

    @BeforeEach
    void setUp() {
        customerMemoMapper = mock(CustomerMemoMapper.class);
        service = new CustomerMemoQueryService(customerMemoMapper);
    }

    @Test
    @DisplayName("getCustomerMemos: mapper list + count 조회 후 PageResponse 반환")
    void getCustomerMemos_returnsPageResponse() {
        // given
        PageRequest page = new PageRequest();
        page.setPage(2);
        page.setSize(10);

        CustomerMemoSearchRequest search = new CustomerMemoSearchRequest();

        SortRequest sort = new SortRequest();
        sort.setSortBy("created_at");
        sort.setDirection("DESC");

        List<CustomerMemoResponse> list = List.of(mock(CustomerMemoResponse.class), mock(CustomerMemoResponse.class));
        when(customerMemoMapper.findCustomerMemos(page, search, sort)).thenReturn(list);
        when(customerMemoMapper.countCustomerMemos(search)).thenReturn(25L);

        // when
        PageResponse<CustomerMemoResponse> res = service.getCustomerMemos(page, search, sort);

        // then
        assertThat(res).isNotNull();
        assertThat(res.getContent()).hasSize(2);
        assertThat(res.getPage()).isEqualTo(2);
        assertThat(res.getSize()).isEqualTo(10);
        assertThat(res.getTotalElements()).isEqualTo(25L);
        assertThat(res.getTotalPages()).isEqualTo((int) Math.ceil(25.0 / 10.0));

        verify(customerMemoMapper).findCustomerMemos(page, search, sort);
        verify(customerMemoMapper).countCustomerMemos(search);
    }

    @Test
    @DisplayName("getCustomerMemoDetail: mapper 단건 조회 그대로 반환")
    void getCustomerMemoDetail_returnsMapperResult() {
        // given
        CustomerMemoSearchRequest search = new CustomerMemoSearchRequest();
        CustomerMemoResponse detail = mock(CustomerMemoResponse.class);
        when(customerMemoMapper.findCustomerMemoDetail(search)).thenReturn(detail);

        // when
        CustomerMemoResponse res = service.getCustomerMemoDetail(search);

        // then
        assertThat(res).isSameAs(detail);
        verify(customerMemoMapper).findCustomerMemoDetail(search);
    }
}
