package com.gaekdam.gaekdambe.customer_service.customer.query.dto.request;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ContractType;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.CustomerStatus;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.NationalityType;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerListSearchRequest extends PageRequest {

    private Long hotelGroupCode; // required

    // 상단 통합검색(이름/연락처/고객코드)
    private String keyword;

    // 상세검색(모달)
    private String customerName;
    private String phoneNumber;
    private String email;
    private Long customerCode;

    // 필터
    private CustomerStatus status;
    private ContractType contractType;
    private NationalityType nationalityType;

    private Long membershipGradeCode;
    private Long loyaltyGradeCode;

    private String inflowChannel;

    // 정렬
    private String sortBy;
    private String direction;
}
