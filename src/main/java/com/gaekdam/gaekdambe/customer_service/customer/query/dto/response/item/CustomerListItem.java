package com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.item;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ContractType;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.CustomerStatus;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.NationalityType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerListItem {

    private Long customerCode;
    private String customerName;

    private String primaryContact;
    private CustomerStatus status;

    private String membershipGrade; // 없으면 "미가입"
    private String loyaltyGrade;    // 없으면 null

    private LocalDate lastUsedDate; // 없으면 null
    private String inflowChannel;   // 없으면 null

    private ContractType contractType;
    private NationalityType nationalityType;
}
