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

    @Column(name = "employee_code", nullable = false)
    private Long employeeCode;

    @Lob
    @Column(name = "customer_memo_content", nullable = false)
    private String customerMemoContent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by_employee_code")
    private Long deletedByEmployeeCode;

    private CustomerMemo(Long customerCode, Long employeeCode, String content, LocalDateTime now) {
        this.customerCode = customerCode;
        this.employeeCode = employeeCode;
        this.customerMemoContent = content;
        this.createdAt = now;
    }

    public static CustomerMemo registerCustomerMemo(Long customerCode, Long employeeCode, String content, LocalDateTime now) {
        return new CustomerMemo(customerCode, employeeCode, content, now);
    }

    public void changeContent(String newContent, LocalDateTime now) {
        this.customerMemoContent = newContent;
        this.updatedAt = now;
    }

    public void delete(Long deletedByEmployeeCode, LocalDateTime now) {
        this.deletedAt = now;
        this.deletedByEmployeeCode = deletedByEmployeeCode;
        this.updatedAt = now;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
