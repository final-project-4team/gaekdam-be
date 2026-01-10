package com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity;

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
        name = "loyalty",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UQ_loyalty_hotel_group_customer",
                        columnNames = {"hotel_group_code", "customer_code"}
                )
        },
        indexes = {
                @Index(name = "IDX_loyalty_customer", columnList = "customer_code"),
                @Index(name = "IDX_loyalty_grade", columnList = "loyalty_grade_code")
        }
)
public class Loyalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loyalty_code", nullable = false)
    private Long loyaltyCode;

    @Column(name = "customer_code", nullable = false)
    private Long customerCode;

    @Column(name = "hotel_group_code", nullable = false)
    private Long hotelGroupCode;

    @Column(name = "loyalty_grade_code", nullable = false)
    private Long loyaltyGradeCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "loyalty_status", nullable = false, length = 30)
    private LoyaltyStatus loyaltyStatus;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private Loyalty(
            Long customerCode,
            Long hotelGroupCode,
            Long loyaltyGradeCode,
            LoyaltyStatus loyaltyStatus,
            LocalDateTime joinedAt,
            LocalDateTime calculatedAt,
            LocalDateTime now
    ) {
        this.customerCode = customerCode;
        this.hotelGroupCode = hotelGroupCode;
        this.loyaltyGradeCode = loyaltyGradeCode;
        this.loyaltyStatus = loyaltyStatus;
        this.joinedAt = joinedAt;
        this.calculatedAt = calculatedAt;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static Loyalty registerLoyalty(
            Long customerCode,
            Long hotelGroupCode,
            Long loyaltyGradeCode,
            LocalDateTime joinedAt,
            LocalDateTime now
    ) {
        return new Loyalty(
                customerCode,
                hotelGroupCode,
                loyaltyGradeCode,
                LoyaltyStatus.ACTIVE,
                joinedAt,
                now,
                now
        );
    }

    public void changeLoyaltyGrade(Long afterLoyaltyGradeCode, LocalDateTime calculatedAt) {
        if (afterLoyaltyGradeCode == null) {
            throw new IllegalArgumentException("afterLoyaltyGradeCode must not be null");
        }
        this.loyaltyGradeCode = afterLoyaltyGradeCode;
        this.calculatedAt = calculatedAt;
        this.updatedAt = calculatedAt;
    }

    public void changeLoyaltyStatus(LoyaltyStatus afterStatus, LocalDateTime now) {
        if (afterStatus == null) {
            throw new IllegalArgumentException("afterStatus must not be null");
        }
        this.loyaltyStatus = afterStatus;
        this.updatedAt = now;
    }
}
