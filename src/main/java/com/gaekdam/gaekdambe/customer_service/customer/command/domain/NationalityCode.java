package com.gaekdam.gaekdambe.customer_service.customer.command.domain;

/**
 * ISO-like nationality codes used across the application for customers.
 * Stored as STRING in DB (e.g. "KR", "CN", "JP").
 */
public enum NationalityCode {
    KR, // Korea                : 한국
    CN, // China                : 중국
    JP, // Japan                : 일본
    TW, // Taiwan               : 대만
    US, // United States        : 미국
    VN, // Vietnam              : 베트남
    TH, // Thailand             : 태국
    PH, // Philippines          : 필리핀
    ID, // Indonesia            : 인도네시아
    IN, // India                : 인도
    OTHER // fallback           : 기타
}
