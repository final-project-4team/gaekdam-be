package com.gaekdam.gaekdambe.customer_service.customer.query.mapper;

import com.gaekdam.gaekdambe.customer_service.customer.query.dto.request.CustomerMemoSearchRequest;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.CustomerMemoResponse;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CustomerMemoMapper {

    List<CustomerMemoResponse> findCustomerMemos(
            @Param("page") PageRequest page,
            @Param("search") CustomerMemoSearchRequest search,
            @Param("sort") SortRequest sort
    );

    long countCustomerMemos(@Param("search") CustomerMemoSearchRequest search);

    CustomerMemoResponse findCustomerMemoDetail(@Param("search") CustomerMemoSearchRequest search);
}
