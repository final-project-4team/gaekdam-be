package com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "customer_memo")
public class CustomerMemo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_memo_code", nullable = false)
    private Long customerMemoCode;

    @Column(name = "customer_code", nullable = false)
    private Long customerCode;

    @Column(name = "user_code", nullable = false)
    private Long userCode;

    @Lob
    @Column(name = "customer_memo_content", nullable = false)
    private String customerMemoContent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private CustomerMemo(Long customerCode, Long userCode, String customerMemoContent, LocalDateTime now) {
        this.customerCode = customerCode;
        this.userCode = userCode;
        this.customerMemoContent = customerMemoContent;
        this.createdAt = now;
    }

    public static CustomerMemo registerCustomerMemo(Long customerCode, Long userCode, String customerMemoContent, LocalDateTime now) {
        return new CustomerMemo(customerCode, userCode, customerMemoContent, now);
    }
}
