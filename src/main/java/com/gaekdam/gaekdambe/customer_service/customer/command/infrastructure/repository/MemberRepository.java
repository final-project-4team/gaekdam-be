package com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByCustomerCode(Long customerCode);
}