package com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ChangeSource;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.CustomerStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "customer_status_history")
public class CustomerStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_status_history_code", nullable = false)
    private Long customerStatusHistoryCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "brfore_status", nullable = false, length = 30) // ERD 오타 그대로
    private CustomerStatus beforeStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "after_status", nullable = false, length = 30)
    private CustomerStatus afterStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_source", length = 20)
    private ChangeSource changeSource;

    @Column(name = "changed_by_user_id")
    private Long changedByUserId;

    @Column(name = "change_reason", length = 255)
    private String changeReason;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "customer_code", nullable = false)
    private Long customerCode;

    private CustomerStatusHistory(
            Long customerCode,
            CustomerStatus beforeStatus,
            CustomerStatus afterStatus,
            ChangeSource changeSource,
            Long changedByUserId,
            String changeReason,
            LocalDateTime changedAt
    ) {
        this.customerCode = customerCode;
        this.beforeStatus = beforeStatus;
        this.afterStatus = afterStatus;
        this.changeSource = changeSource;
        this.changedByUserId = changedByUserId;
        this.changeReason = changeReason;
        this.changedAt = changedAt;
    }

    public static CustomerStatusHistory recordCustomerStatusChange(
            Long customerCode,
            CustomerStatus beforeStatus,
            CustomerStatus afterStatus,
            ChangeSource changeSource,
            Long changedByUserId,
            String changeReason,
            LocalDateTime changedAt
    ) {
        return new CustomerStatusHistory(
                customerCode,
                beforeStatus,
                afterStatus,
                changeSource,
                changedByUserId,
                changeReason,
                changedAt
        );
    }
}
