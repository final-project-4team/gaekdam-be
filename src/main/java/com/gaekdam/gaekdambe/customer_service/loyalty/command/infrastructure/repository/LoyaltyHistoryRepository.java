package com.gaekdam.gaekdambe.customer_service.loyalty.command.infrastructure.repository;

import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.LoyaltyHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoyaltyHistoryRepository extends JpaRepository<LoyaltyHistory, Long> {
}
