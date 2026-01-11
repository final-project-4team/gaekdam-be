package com.gaekdam.gaekdambe.customer_service.customer.query.mapper;

import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CustomerMapper {

    // 고객 존재 여부만 확인
    boolean existsCustomer(CustomerDetailCondition condition);

    // 고객 목록 조회
    List<CustomerListRow> findCustomerList(CustomerListCondition condition);

    // 고객 상세
    CustomerDetailRow findCustomerDetail(CustomerDetailCondition condition);

    // 고객 연락처 리스트 조회
    List<CustomerContactRow> findCustomerContacts(CustomerDetailCondition condition);

    // 고객 상태만 조회
    CustomerStatusRow findCustomerStatus(CustomerDetailCondition condition);

    // 고객 상태 변경 이력 조회
    List<CustomerStatusHistoryRow> findCustomerStatusHistories(CustomerDetailCondition condition);
}
