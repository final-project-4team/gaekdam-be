// src/main/java/com/gaekdam/gaekdambe/customer_service/customer/query/mapper/CustomerSnapshotMapper.java
package com.gaekdam.gaekdambe.customer_service.customer.query.mapper;

import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.row.CustomerSnapshotRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CustomerSnapshotMapper {

    CustomerSnapshotRow findCustomerSnapshot(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("customerCode") Long customerCode
    );
}
