package com.gaekdam.gaekdambe.customer_service.customer.query.dto.response;

import lombok.Getter;

@Getter
public class CustomerBasicRow {

    private Long customerCode;
    private byte[] customerNameEnc;
    private byte[] phoneEnc;
    private byte[] dekEnc;
}
