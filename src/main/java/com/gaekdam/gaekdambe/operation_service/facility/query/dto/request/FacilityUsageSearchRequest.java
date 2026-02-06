package com.gaekdam.gaekdambe.operation_service.facility.query.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FacilityUsageSearchRequest {

    /* =========================
       필수 스코프
       ========================= */
    private Long hotelGroupCode;

    /* =========================
       필터
       ========================= */
    private Long propertyCode;
    private Long stayCode;
    private Long facilityCode;

    private String customerNameHash;
    private String stayCodeLike;

    private String usageType;
    private String priceSource;

    /* =========================
       날짜 범위 (핵심)
       ========================= */
    private LocalDateTime startAt; // 조회 시작 (inclusive)
    private LocalDateTime endAt;   // 조회 종료 (exclusive)
}
