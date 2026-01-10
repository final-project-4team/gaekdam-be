package com.gaekdam.gaekdambe.customer_service.loyalty.command.infrastructure.repository;

import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.LoyaltyGrade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoyaltyGradeRepository extends JpaRepository<LoyaltyGrade, Long> {
}
