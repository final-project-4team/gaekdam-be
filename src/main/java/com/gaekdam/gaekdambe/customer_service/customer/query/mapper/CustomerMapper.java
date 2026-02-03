package com.gaekdam.gaekdambe.customer_service.customer.query.mapper;

import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.CustomerBasicRow;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.condition.CustomerListSearchParam;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.row.*;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CustomerMapper {

    List<CustomerListRow> findCustomers(
            @Param("page") PageRequest page,
            @Param("search") CustomerListSearchParam search,
            @Param("sort") SortRequest sort
    );

    long countCustomers(@Param("search") CustomerListSearchParam search);

    CustomerDetailRow findCustomerDetail(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("customerCode") Long customerCode
    );

    List<CustomerContactRow> findCustomerContacts(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("customerCode") Long customerCode
    );

    CustomerStatusRow findCustomerStatus(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("customerCode") Long customerCode
    );

    List<CustomerStatusHistoryRow> findCustomerStatusHistories(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("customerCode") Long customerCode,
            @Param("page") PageRequest page,
            @Param("sort") SortRequest sort
    );

    long countCustomerStatusHistories(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("customerCode") Long customerCode
    );

    List<CustomerContactRow> findCustomerMarketingConsents(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("customerCode") Long customerCode
    );


    CustomerBasicRow findCustomerBasic(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("customerCode") Long customerCode
    );

}
