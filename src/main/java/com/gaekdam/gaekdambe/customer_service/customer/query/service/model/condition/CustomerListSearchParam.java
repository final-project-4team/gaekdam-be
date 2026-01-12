package com.gaekdam.gaekdambe.customer_service.customer.query.service.model.condition;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ContractType;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.CustomerStatus;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.NationalityType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerListSearchParam {

    private Long hotelGroupCode;

    private Long customerCode;
    private String customerNameHash;
    private String phoneHash;
    private String emailHash;

    private CustomerStatus status;
    private ContractType contractType;
    private NationalityType nationalityType;

    private Long membershipGradeCode;
    private Long loyaltyGradeCode;

    private String inflowChannel;
}
