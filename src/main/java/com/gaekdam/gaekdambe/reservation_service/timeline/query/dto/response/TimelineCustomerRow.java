package com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response;

import lombok.Getter;

@Getter
public class TimelineCustomerRow {

    private Long customerCode;
    private byte[] customerNameEnc;
    private byte[] phoneEnc;
    private byte[] dekEnc;
}
