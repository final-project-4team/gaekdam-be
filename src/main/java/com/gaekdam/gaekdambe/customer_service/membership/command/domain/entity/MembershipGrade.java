package com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "membership_grade",
        uniqueConstraints = {
                // 호텔그룹별 등급명 중복 방지
                @UniqueConstraint(
                        name = "UQ_membership_grade_hotel_group_name",
                        columnNames = {"hotel_group_code", "grade_name"}
                )
        },
        indexes = {
                // 목록/정렬 최적화
                @Index(name = "IDX_membership_grade_hotel_group", columnList = "hotel_group_code"),
                @Index(name = "IDX_membership_grade_tier", columnList = "hotel_group_code,tier_level")
        }
)
public class MembershipGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "membership_grade_code", nullable = false)
    private Long membershipGradeCode;

    @Column(name = "hotel_group_code", nullable = false)
    private Long hotelGroupCode;

    @Column(name = "grade_name", nullable = false, length = 50)
    private String gradeName;

    @Column(name = "tier_level", nullable = false)
    private Long tierLevel;

    @Column(name = "tier_comment", nullable = false, length = 255)
    private String tierComment;

    @Column(name = "calculation_standard", nullable = false, columnDefinition = "TEXT")
    private String calculationStandard;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private MembershipGrade(
            Long hotelGroupCode,
            String gradeName,
            Long tierLevel,
            String tierComment,
            String calculationStandard,
            Boolean isActive,
            LocalDateTime now
    ) {
        if (gradeName == null || gradeName.isBlank()) {
            throw new IllegalArgumentException("gradeName must not be blank");
        }
        if (tierComment == null || tierComment.isBlank()) {
            throw new IllegalArgumentException("tierComment must not be blank");
        }
        if (calculationStandard == null || calculationStandard.isBlank()) { // ✅ 변경
            throw new IllegalArgumentException("calculationStandard must not be blank");
        }

        this.hotelGroupCode = hotelGroupCode;
        this.gradeName = gradeName.trim();
        this.tierLevel = tierLevel;
        this.tierComment = tierComment;
        this.calculationStandard = calculationStandard;
        this.isActive = (isActive != null) ? isActive : Boolean.TRUE;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static MembershipGrade registerMembershipGrade(
            Long hotelGroupCode,
            String gradeName,
            Long tierLevel,
            String tierComment,
            String calculationStandard,
            Boolean isActive,
            LocalDateTime now
    ) {
        return new MembershipGrade(
                hotelGroupCode,
                gradeName,
                tierLevel,
                tierComment,
                calculationStandard,
                isActive,
                now
        );
    }

    public void changeActive(Boolean isActive, LocalDateTime now) {
        this.isActive = isActive;
        this.updatedAt = now;
    }

    // 산정 기준 변경 메서드도 제공 (관리자 기능 대비)
    public void changeCalculationStandard(String calculationStandard, LocalDateTime now) {
        if (calculationStandard == null || calculationStandard.isBlank()) {
            throw new IllegalArgumentException("calculationStandard must not be blank");
        }
        this.calculationStandard = calculationStandard;
        this.updatedAt = now;
    }
}
