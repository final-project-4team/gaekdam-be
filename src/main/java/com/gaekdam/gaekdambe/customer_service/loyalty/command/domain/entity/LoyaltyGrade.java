package com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity;

import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.LoyaltyGradeName;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "loyalty_grade",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UQ_loyalty_grade_hotel_group_name",
                        columnNames = {"hotel_group_code", "grade_name"}
                )
        },
        indexes = {
                @Index(name = "IDX_loyalty_grade_hotel_group", columnList = "hotel_group_code"),
                @Index(name = "IDX_loyalty_grade_tier", columnList = "hotel_group_code,tier_level")
        }
)
public class LoyaltyGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loyalty_grade_code", nullable = false)
    private Long loyaltyGradeCode;

    @Column(name = "hotel_group_code", nullable = false)
    private Long hotelGroupCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade_name", nullable = false, length = 50)
    private LoyaltyGradeName gradeName;

    @Column(name = "tier_level", nullable = false)
    private Long tierLevel;

    @Column(name = "calculation_standard", nullable = false)
    private String calculationStandard;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private LoyaltyGrade(
            Long hotelGroupCode,
            LoyaltyGradeName gradeName,
            Long tierLevel,
            String calculationStandard,
            Boolean isActive,
            LocalDateTime now
    ) {
        this.hotelGroupCode = hotelGroupCode;
        this.gradeName = gradeName;
        this.tierLevel = tierLevel;
        this.calculationStandard = calculationStandard;
        this.isActive = isActive;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static LoyaltyGrade registerLoyaltyGrade(
            Long hotelGroupCode,
            LoyaltyGradeName gradeName,
            Long tierLevel,
            String calculationStandard,
            Boolean isActive,
            LocalDateTime now
    ) {
        return new LoyaltyGrade(
                hotelGroupCode,
                gradeName,
                tierLevel,
                calculationStandard,
                isActive,
                now
        );
    }

    public void changeActive(Boolean isActive, LocalDateTime now) {
        this.isActive = isActive;
        this.updatedAt = now;
    }

    public void changeCalculationStandard(String calculationStandard, LocalDateTime now) {
        this.calculationStandard = calculationStandard;
        this.updatedAt = now;
    }
}
