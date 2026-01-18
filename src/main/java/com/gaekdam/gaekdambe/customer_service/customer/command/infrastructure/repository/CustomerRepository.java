package com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCustomerCodeAndHotelGroupCode(Long customerCode, Long hotelGroupCode);
}
