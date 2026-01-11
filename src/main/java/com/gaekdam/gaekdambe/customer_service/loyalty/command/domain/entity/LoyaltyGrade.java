package com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity;

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

    @Column(name = "grade_name", nullable = false, length = 50)
    private String gradeName;

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
            String gradeName,
            Long tierLevel,
            String calculationStandard,
            Boolean isActive,
            LocalDateTime now
    ) {
        if (gradeName == null || gradeName.isBlank()) {
            throw new IllegalArgumentException("gradeName must not be blank");
        }
        if (calculationStandard == null || calculationStandard.isBlank()) {
            throw new IllegalArgumentException("calculationStandard must not be blank");
        }

        this.hotelGroupCode = hotelGroupCode;
        this.gradeName = gradeName.trim();
        this.tierLevel = tierLevel;
        this.calculationStandard = calculationStandard;
        this.isActive = (isActive != null) ? isActive : Boolean.TRUE;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static LoyaltyGrade registerLoyaltyGrade(
            Long hotelGroupCode,
            String gradeName,
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
        if (calculationStandard == null || calculationStandard.isBlank()) {
            throw new IllegalArgumentException("calculationStandard must not be blank");
        }
        this.calculationStandard = calculationStandard;
        this.updatedAt = now;
    }
}
