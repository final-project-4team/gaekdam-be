// src/main/java/com/gaekdam/gaekdambe/customer_service/customer/query/dto/response/item/CustomerTimelineItem.java
package com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.item;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CustomerTimelineItem {

    /** MEMO / RESERVATION_CREATED / RESERVATION_CANCELED / STAY / INQUIRY_CREATED / INQUIRY_ANSWERED / MEMBERSHIP / LOYALTY / CUSTOMER_STATUS */
    private String eventType;

    private LocalDateTime occurredAt;

    /** 이벤트 원본 PK(메모코드, 예약코드 등) */
    private Long refId;

    /** 화면 표시용 타이틀 */
    private String title;

    /** 한 줄 요약 */
    private String summary;
}
