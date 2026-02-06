package com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity;

import com.gaekdam.gaekdambe.customer_service.membership.command.domain.MembershipStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "membership")
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "membership_code", nullable = false)
    private Long membershipCode;

    @Column(name = "customer_code", nullable = false)
    private Long customerCode;

    @Column(name = "hotel_group_code", nullable = false)
    private Long hotelGroupCode;

    @Column(name = "membership_grade_code", nullable = false)
    private Long membershipGradeCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_status", nullable = false, length = 30)
    private MembershipStatus membershipStatus;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private Membership(
            Long customerCode,
            Long hotelGroupCode,
            Long membershipGradeCode,
            MembershipStatus membershipStatus,
            LocalDateTime joinedAt,
            LocalDateTime now) {
        this.customerCode = customerCode;
        this.hotelGroupCode = hotelGroupCode;
        this.membershipGradeCode = membershipGradeCode;
        this.membershipStatus = membershipStatus;
        this.joinedAt = joinedAt;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static Membership registerMembership(
            Long customerCode,
            Long hotelGroupCode,
            Long membershipGradeCode,
            LocalDateTime joinedAt,
            LocalDateTime now) {
        LocalDateTime expiredAt = now.withMonth(12).withDayOfMonth(31).withHour(23).withMinute(59).withSecond(59);
        Membership membership = new Membership(customerCode, hotelGroupCode, membershipGradeCode,
                MembershipStatus.ACTIVE, joinedAt, now);
        membership.expiredAt = expiredAt;
        return membership;
    }

    public void changeMembershipStatus(MembershipStatus afterStatus, LocalDateTime now) {
        this.membershipStatus = afterStatus;
        this.updatedAt = now;
    }

    // 맴버십 수정
    public void changeMembership(
            Long afterMembershipGradeCode,
            MembershipStatus afterStatus,
            LocalDateTime afterExpiredAt,
            LocalDateTime now) {
        this.membershipGradeCode = afterMembershipGradeCode;
        this.membershipStatus = afterStatus;
        this.expiredAt = afterExpiredAt;
        this.calculatedAt = now;
        this.updatedAt = now;
    }

}
