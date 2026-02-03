// src/main/java/com/gaekdam/gaekdambe/customer_service/customer/query/dto/response/CustomerSnapshotResponse.java
package com.gaekdam.gaekdambe.customer_service.customer.query.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class CustomerSnapshotResponse {

    private Long customerCode;

    /** 총 이용횟수: Stay 기준(완료) */
    private long totalStayCount;

    /** LTV: Reservation.total_price 합(취소 제외) */
    private BigDecimal ltvAmount;

    /** 최근 이용일: 최근 체크아웃일(없으면 체크인/생성 fallback) */
    private LocalDateTime lastUsedAt;

    /** 미해결 이슈: Inquiry IN_PROGRESS 건수 */
    private long unresolvedInquiryCount;
}
