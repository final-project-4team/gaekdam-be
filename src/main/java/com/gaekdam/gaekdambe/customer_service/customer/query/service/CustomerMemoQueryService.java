package com.gaekdam.gaekdambe.customer_service.customer.query.service;

import com.gaekdam.gaekdambe.customer_service.customer.query.dto.request.CustomerMemoSearchRequest;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.CustomerMemoResponse;
import com.gaekdam.gaekdambe.customer_service.customer.query.mapper.CustomerMemoMapper;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerMemoQueryService {

  private final CustomerMemoMapper customerMemoMapper;

  public PageResponse<CustomerMemoResponse> getCustomerMemos(PageRequest page,
      CustomerMemoSearchRequest search, SortRequest sort) {
    List<CustomerMemoResponse> list = customerMemoMapper.findCustomerMemos(page, search, sort);
    long total = customerMemoMapper.countCustomerMemos(search);

    return new PageResponse<>(list, page.getPage(), page.getSize(), total);
  }


  public CustomerMemoResponse getCustomerMemoDetail(CustomerMemoSearchRequest search) {
    return customerMemoMapper.findCustomerMemoDetail(search);
  }
}
