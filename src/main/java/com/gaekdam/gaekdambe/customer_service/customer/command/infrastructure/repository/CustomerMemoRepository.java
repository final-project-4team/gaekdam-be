package com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.CustomerMemo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerMemoRepository extends JpaRepository<CustomerMemo, Long> {
}