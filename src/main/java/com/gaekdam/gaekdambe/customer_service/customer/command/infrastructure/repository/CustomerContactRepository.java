package com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.CustomerContact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerContactRepository extends JpaRepository<CustomerContact, Long> {
}
