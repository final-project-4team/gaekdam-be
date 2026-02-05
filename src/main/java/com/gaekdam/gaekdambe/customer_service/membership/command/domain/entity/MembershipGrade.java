package com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity;

import com.gaekdam.gaekdambe.customer_service.membership.command.domain.MembershipGradeStatus;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "membership_grade", uniqueConstraints = {
    // 호텔그룹별 등급명 중복 방지
    @UniqueConstraint(name = "UQ_membership_grade_hotel_group_name", columnNames = { "hotel_group_code", "grade_name" })
}, indexes = {
    // 목록/정렬 최적화
    @Index(name = "IDX_membership_grade_hotel_group", columnList = "hotel_group_code"),
    @Index(name = "IDX_membership_grade_tier", columnList = "hotel_group_code,tier_level")
})
@EntityListeners(AuditingEntityListener.class)
public class MembershipGrade {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "membership_grade_code", nullable = false)
  private Long membershipGradeCode;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "hotel_group_code", nullable = false)
  private HotelGroup hotelGroup;

  @Column(name = "grade_name", nullable = false, length = 50)
  private String gradeName;

  @Column(name = "tier_level", nullable = false)
  private Long tierLevel;

  @Column(name = "tier_comment", nullable = false, length = 255)
  private String tierComment;

  @Column(name = "calculation_amount")
  private Long calculationAmount;

  @Column(name = "calculation_count")
  private Integer calculationCount;

  @Column(name = "calculation_term_month", nullable = false)
  private Integer calculationTermMonth;

  @Column(name = "calculation_renewal_day", nullable = false)
  private Integer calculationRenewalDay;

  @Enumerated(EnumType.STRING)
  @Column(name = "membership_grade_status", nullable = false)
  private MembershipGradeStatus membershipGradeStatus;

  @CreatedDate
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  private MembershipGrade(
      HotelGroup hotelGroup,
      String gradeName,
      Long tierLevel,
      String tierComment,
      Long calculationAmount,
      Integer calculationCount) {
    if (gradeName == null || gradeName.isBlank()) {
      throw new IllegalArgumentException("gradeName must not be blank");
    }
    if (tierComment == null || tierComment.isBlank()) {
      throw new IllegalArgumentException("tierComment must not be blank");
    }


    this.hotelGroup = hotelGroup;
    this.gradeName = gradeName.trim();
    this.tierLevel = tierLevel;
    this.tierComment = tierComment;
    this.calculationAmount = calculationAmount;
    this.calculationCount = calculationCount;
    this.calculationTermMonth = 12;
    this.calculationRenewalDay = 1;
    this.membershipGradeStatus = MembershipGradeStatus.ACTIVE;
  }

  public static MembershipGrade registerMembershipGrade(
      HotelGroup hotelGroup,
      String gradeName,
      Long tierLevel,
      String tierComment,
      Long calculationAmount,
      Integer calculationCount) {
    return new MembershipGrade(
        hotelGroup,
        gradeName,
        tierLevel,
        tierComment,
        calculationAmount,
        calculationCount);
  }

  public void deleteMemberShipGradeStatus() {
    this.membershipGradeStatus = MembershipGradeStatus.INACTIVE;
  }

  public void activeMemberShipGradeStatus() {
    this.membershipGradeStatus = MembershipGradeStatus.ACTIVE;
  }

  public void update(
      String gradeName,
      Long tierLevel,
      String tierComment,
      Long calculationAmount,
      Integer calculationCount) {
    if (gradeName == null || gradeName.isBlank()) {
      throw new IllegalArgumentException("gradeName must not be blank");
    }
    if (tierComment == null || tierComment.isBlank()) {
      throw new IllegalArgumentException("tierComment must not be blank");
    }

    this.gradeName = gradeName;
    this.tierLevel = tierLevel;
    this.tierComment = tierComment;
    this.calculationAmount = calculationAmount;
    this.calculationCount = calculationCount;
  }
}
