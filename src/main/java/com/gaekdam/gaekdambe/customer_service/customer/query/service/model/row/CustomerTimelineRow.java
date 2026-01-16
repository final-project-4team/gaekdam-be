// src/main/java/com/gaekdam/gaekdambe/customer_service/customer/query/service/model/row/CustomerTimelineEventRow.java
package com.gaekdam.gaekdambe.customer_service.customer.query.service.model.row;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CustomerTimelineRow {

    private String eventType;
    private LocalDateTime occurredAt;
    private Long refId;
    private String title;
    private String summary;
}
