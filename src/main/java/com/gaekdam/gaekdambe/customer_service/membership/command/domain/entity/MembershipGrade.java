package com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "membership_grade")
public class MembershipGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "membership_grade_code", nullable = false)
    private Long membershipGradeCode;

    @Column(name = "hotel_group_code", nullable = false)
    private Long hotelGroupCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade_name", nullable = false, length = 30)
    private MembershipGradeName gradeName;

    @Column(name = "tier_level", nullable = false)
    private Long tierLevel;

    @Column(name = "tier_comment", nullable = false, length = 255)
    private String tierComment;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private MembershipGrade(
            Long hotelGroupCode,
            MembershipGradeName gradeName,
            Long tierLevel,
            String tierComment,
            Boolean isActive,
            LocalDateTime now
    ) {
        this.hotelGroupCode = hotelGroupCode;
        this.gradeName = gradeName;
        this.tierLevel = tierLevel;
        this.tierComment = tierComment;
        this.isActive = isActive != null ? isActive : Boolean.TRUE;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static MembershipGrade registerMembershipGrade(
            Long hotelGroupCode,
            MembershipGradeName gradeName,
            Long tierLevel,
            String tierComment,
            Boolean isActive,
            LocalDateTime now
    ) {
        return new MembershipGrade(hotelGroupCode, gradeName, tierLevel, tierComment, isActive, now);
    }

    public void changeActive(Boolean isActive, LocalDateTime now) {
        this.isActive = isActive;
        this.updatedAt = now;
    }
}
