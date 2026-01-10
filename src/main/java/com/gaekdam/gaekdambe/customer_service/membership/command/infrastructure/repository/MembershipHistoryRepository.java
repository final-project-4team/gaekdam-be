package com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository;

import com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity.MembershipHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipHistoryRepository extends JpaRepository<MembershipHistory,Long > {
}
