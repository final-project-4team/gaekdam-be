package com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerContactItem {

    private Long contactCode;
    private String contactType;   // PHONE / EMAIL
    private String contactValue;  // 복호화 or 마스킹 값

    private Boolean isPrimary;
    private Boolean marketingOptIn;
    private LocalDateTime consentAt;
}
