package com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.CustomerMemo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerMemoRepository extends JpaRepository<CustomerMemo, Long> {

    Optional<CustomerMemo> findByCustomerMemoCodeAndCustomerCode(Long memoCode, Long customerCode);
}
