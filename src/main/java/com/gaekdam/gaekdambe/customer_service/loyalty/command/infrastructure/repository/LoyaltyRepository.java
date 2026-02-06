package com.gaekdam.gaekdambe.customer_service.loyalty.command.infrastructure.repository;

import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.Loyalty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoyaltyRepository extends JpaRepository<Loyalty, Long> {
    Optional<Loyalty> findByHotelGroupCodeAndCustomerCode(Long hotelGroupCode, Long customerCode);

    java.util.List<Loyalty> findAllByHotelGroupCode(Long hotelGroupCode);
}
