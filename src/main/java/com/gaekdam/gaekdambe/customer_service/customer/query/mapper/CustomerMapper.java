package com.gaekdam.gaekdambe.customer_service.customer.query.mapper;

import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.CustomerUnifiedListCondition;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.CustomerUnifiedRow;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CustomerMapper {
    long countCustomerUnifiedList(CustomerUnifiedListCondition condition);
    List<CustomerUnifiedRow> findCustomerUnifiedList(CustomerUnifiedListCondition condition);
}