package com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ChangeSource;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.LoyaltyStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "loyalty_history",
        indexes = {
                @Index(name = "IDX_loyalty_history_customer", columnList = "customer_code"),
                @Index(name = "IDX_loyalty_history_loyalty", columnList = "loyalty_code")
        }
)
public class LoyaltyHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loyalty_history_code", nullable = false)
    private Long loyaltyHistoryCode;

    @Column(name = "customer_code", nullable = false)
    private Long customerCode;

    @Column(name = "loyalty_code", nullable = false)
    private Long loyaltyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_source", nullable = false, length = 20)
    private ChangeSource changeSource;

    @Column(name = "employee_code")
    private Long changedByEmployeeCode;

    @Column(name = "change_reason", length = 255)
    private String changeReason;

    @Column(name = "before_loyalty_grade_code")
    private Long beforeLoyaltyGradeCode;

    @Column(name = "after_loyalty_grade_code", nullable = false)
    private Long afterLoyaltyGradeCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "before_status", length = 30)
    private LoyaltyStatus beforeStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "after_status", nullable = false, length = 30)
    private LoyaltyStatus afterStatus;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    private LoyaltyHistory(
            Long customerCode,
            Long loyaltyCode,
            ChangeSource changeSource,
            Long changedByEmployeeCode,
            String changeReason,
            Long beforeLoyaltyGradeCode,
            Long afterLoyaltyGradeCode,
            LoyaltyStatus beforeStatus,
            LoyaltyStatus afterStatus,
            LocalDateTime changedAt
    ) {
        this.customerCode = customerCode;
        this.loyaltyCode = loyaltyCode;
        this.changeSource = changeSource;
        this.changedByEmployeeCode = changedByEmployeeCode;
        this.changeReason = changeReason;
        this.beforeLoyaltyGradeCode = beforeLoyaltyGradeCode;
        this.afterLoyaltyGradeCode = afterLoyaltyGradeCode;
        this.beforeStatus = beforeStatus;
        this.afterStatus = afterStatus;
        this.changedAt = changedAt;
    }

    public static LoyaltyHistory recordLoyaltyChange(
            Long customerCode,
            Long loyaltyCode,
            ChangeSource changeSource,
            Long changedByEmployeeCode,
            String changeReason,
            Long beforeLoyaltyGradeCode,
            Long afterLoyaltyGradeCode,
            LoyaltyStatus beforeStatus,
            LoyaltyStatus afterStatus,
            LocalDateTime changedAt
    ) {
        return new LoyaltyHistory(
                customerCode,
                loyaltyCode,
                changeSource,
                changedByEmployeeCode,
                changeReason,
                beforeLoyaltyGradeCode,
                afterLoyaltyGradeCode,
                beforeStatus,
                afterStatus,
                changedAt
        );
    }
}
