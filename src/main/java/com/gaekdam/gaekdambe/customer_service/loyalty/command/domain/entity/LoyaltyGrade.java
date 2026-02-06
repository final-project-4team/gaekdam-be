package com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity;

import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.LoyaltyGradeStatus;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "loyalty_grade", uniqueConstraints = {
    @UniqueConstraint(name = "UQ_loyalty_grade_hotel_group_name", columnNames = { "hotel_group_code",
        "loyalty_grade_name" } //
    )
}, indexes = {
    @Index(name = "IDX_loyalty_grade_hotel_group", columnList = "hotel_group_code"),
    @Index(name = "IDX_loyalty_grade_tier", columnList = "hotel_group_code,loyalty_tier_level") //
})
@EntityListeners(AuditingEntityListener.class)
public class LoyaltyGrade {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "loyalty_grade_code", nullable = false)
  private Long loyaltyGradeCode;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "hotel_group_code", nullable = false)
  private HotelGroup hotelGroup;

  @Column(name = "loyalty_grade_name", nullable = false, length = 50)
  private String loyaltyGradeName;

  @Column(name = "loyalty_tier_level", nullable = false)
  private Long loyaltyTierLevel;

  @Column(name = "loyalty_tier_comment", nullable = false, length = 255)
  private String loyaltyTierComment;

  @Column(name = "loyalty_calculation_amount")
  private Long loyaltyCalculationAmount;

  @Column(name = "loyalty_calculation_count")
  private Integer loyaltyCalculationCount;

  @Column(name = "loyalty_calculation_term_month", nullable = false)
  private Integer loyaltyCalculationTermMonth;

  @Column(name = "loyalty_calculation_renewal_day", nullable = false)
  private Integer loyaltyCalculationRenewalDay;

  @Enumerated(EnumType.STRING)
  @Column(name = "loyalty_grade_status", nullable = false)
  private LoyaltyGradeStatus loyaltyGradeStatus;

  @CreatedDate
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  private LoyaltyGrade(
      HotelGroup hotelGroup,
      String loyaltyGradeName,
      Long loyaltyTierLevel,
      String loyaltyTierComment,
      Long loyaltyCalculationAmount,
      Integer loyaltyCalculationCount) {
    if (loyaltyGradeName == null || loyaltyGradeName.isBlank()) {
      throw new IllegalArgumentException("gradeName must not be blank");
    }
    if (loyaltyTierComment == null || loyaltyTierComment.isBlank()) {
      throw new IllegalArgumentException("tierComment must not be blank");
    }

    this.hotelGroup = hotelGroup;
    this.loyaltyGradeName = loyaltyGradeName.trim();
    this.loyaltyTierLevel = loyaltyTierLevel;
    this.loyaltyTierComment = loyaltyTierComment;
    this.loyaltyCalculationAmount = loyaltyCalculationAmount;
    this.loyaltyCalculationCount = loyaltyCalculationCount;
    this.loyaltyCalculationTermMonth = 12;
    this.loyaltyCalculationRenewalDay = 1;
    this.loyaltyGradeStatus = LoyaltyGradeStatus.ACTIVE;
  }

  public static LoyaltyGrade registerLoyaltyGrade(
      HotelGroup hotelGroup,
      String loyaltyGradeName,
      Long loyaltyTierLevel,
      String loyaltyTierComment,
      Long loyaltyCalculationAmount,
      Integer loyaltyCalculationCount) {
    return new LoyaltyGrade(
        hotelGroup,
        loyaltyGradeName,
        loyaltyTierLevel,
        loyaltyTierComment,
        loyaltyCalculationAmount,
        loyaltyCalculationCount);
  }

  public void deleteLoyaltyGradeStatus() {
    this.loyaltyGradeStatus = LoyaltyGradeStatus.INACTIVE;
  }

  public void activeLoyaltyGradeStatus() {
    this.loyaltyGradeStatus = LoyaltyGradeStatus.ACTIVE;
  }

  public void update(
      String loyaltyGradeName,
      Long loyaltyTierLevel,
      String loyaltyTierComment,
      Long loyaltyCalculationAmount,
      Integer loyaltyCalculationCount) {
    if (loyaltyGradeName == null || loyaltyGradeName.isBlank()) {
      throw new IllegalArgumentException("gradeName must not be blank");
    }
    if (loyaltyTierComment == null || loyaltyTierComment.isBlank()) {
      throw new IllegalArgumentException("tierComment must not be blank");
    }

    this.loyaltyGradeName = loyaltyGradeName.trim();
    this.loyaltyTierLevel = loyaltyTierLevel;
    this.loyaltyTierComment = loyaltyTierComment;
    this.loyaltyCalculationAmount = loyaltyCalculationAmount;
    this.loyaltyCalculationCount = loyaltyCalculationCount;
  }
}
