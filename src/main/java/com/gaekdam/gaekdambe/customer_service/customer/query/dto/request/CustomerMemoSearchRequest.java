package com.gaekdam.gaekdambe.customer_service.customer.query.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class CustomerMemoSearchRequest {

    private Long hotelGroupCode;
    private Long customerCode;
    private Long customerMemoCode;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime toDate;
}
