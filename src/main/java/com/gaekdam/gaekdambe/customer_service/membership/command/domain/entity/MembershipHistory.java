package com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ChangeSource;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.MembershipStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "membership_history")
public class MembershipHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "membership_history_code", nullable = false)
    private Long membershipHistoryCode;

    @Column(name = "customer_code", nullable = false)
    private Long customerCode;

    @Column(name = "membership_code", nullable = false)
    private Long membershipCode;


    @Enumerated(EnumType.STRING)
    @Column(name = "change_source", nullable = false, length = 20)
    private ChangeSource changeSource;

    @Column(name = "employee_code")
    private Long changedByEmployeeCode;

    @Column(name = "change_reason", length = 255)
    private String changeReason;

    @Column(name = "before_grade", length = 30)
    private String beforeGrade;

    @Column(name = "after_grade", nullable = false, length = 30)
    private String afterGrade;

    @Enumerated(EnumType.STRING)
    @Column(name = "before_status", length = 30)
    private MembershipStatus beforeStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "after_status", nullable = false, length = 30)
    private MembershipStatus afterStatus;

    @Column(name = "before_expires_at")
    private LocalDateTime beforeExpiresAt;

    @Column(name = "after_expires_at")
    private LocalDateTime afterExpiresAt;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "membership_grade_code", nullable = false)
    private Long membershipGradeCode;

    private MembershipHistory(
            Long customerCode,
            Long membershipCode,
            ChangeSource changeSource,
            Long changedByEmployeeCode,
            String changeReason,
            String beforeGrade,
            String afterGrade,
            MembershipStatus beforeStatus,
            MembershipStatus afterStatus,
            LocalDateTime beforeExpiresAt,
            LocalDateTime afterExpiresAt,
            LocalDateTime changedAt,
            Long membershipGradeCode
    ) {
        this.customerCode = customerCode;
        this.membershipCode = membershipCode;
        this.changeSource = changeSource;
        this.changedByEmployeeCode = changedByEmployeeCode;
        this.changeReason = changeReason;
        this.beforeGrade = beforeGrade;
        this.afterGrade = afterGrade;
        this.beforeStatus = beforeStatus;
        this.afterStatus = afterStatus;
        this.beforeExpiresAt = beforeExpiresAt;
        this.afterExpiresAt = afterExpiresAt;
        this.changedAt = changedAt;
        this.membershipGradeCode = membershipGradeCode;
    }

    public static MembershipHistory recordMembershipChange(
            Long customerCode,
            Long membershipCode,
            ChangeSource changeSource,
            Long changedByEmployeeCode,
            String changeReason,
            String beforeGrade,
            String afterGrade,
            MembershipStatus beforeStatus,
            MembershipStatus afterStatus,
            LocalDateTime beforeExpiresAt,
            LocalDateTime afterExpiresAt,
            LocalDateTime changedAt,
            Long membershipGradeCode
    ) {
        return new MembershipHistory(
                customerCode,
                membershipCode,
                changeSource,
                changedByEmployeeCode,
                changeReason,
                beforeGrade,
                afterGrade,
                beforeStatus,
                afterStatus,
                beforeExpiresAt,
                afterExpiresAt,
                changedAt,
                membershipGradeCode
        );
    }

}
