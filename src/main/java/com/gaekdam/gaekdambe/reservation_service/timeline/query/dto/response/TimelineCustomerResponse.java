package com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TimelineCustomerResponse {

    private Long customerCode;
    private String customerName;
    private String phone;
}
